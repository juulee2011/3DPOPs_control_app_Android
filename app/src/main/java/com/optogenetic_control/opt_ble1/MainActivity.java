package com.optogenetic_control.wpt_ble1;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import com.optogenetic_control.wpt_ble1.recyclerViewAdapter.ActionModeController;
import com.optogenetic_control.wpt_ble1.recyclerViewAdapter.Item;
import com.optogenetic_control.wpt_ble1.recyclerViewAdapter.ItemListAdapter;
import com.optogenetic_control.wpt_ble1.recyclerViewAdapter.MyItemKeyProvider;
import com.optogenetic_control.wpt_ble1.recyclerViewAdapter.MyItemLookup;

import java.util.ArrayList;
import java.util.Iterator;

import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.OnItemActivatedListener;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private String TAG=MainActivity.class.getSimpleName();
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private boolean mExit = true;
    private Handler mHandler;

    // Stops scanning after 10 seconds.
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 30000;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;

    //RecyclerView Declarations
    SelectionTracker selectionTracker;
    private RecyclerView itemListView;
    private ItemListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ActionMode actionMode;
    private static Menu menuPrivate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            requestPermissions(
//                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
//                                PERMISSION_REQUEST_COARSE_LOCATION);
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    0);
        }

        // Initializes layout manager
        mLayoutManager = new LinearLayoutManager(this);

        // Initializes RecyclerView and sets custom adapter
        itemListView = findViewById(R.id.recyclerview_items);
        itemListView.setLayoutManager(mLayoutManager);
        mAdapter =new ItemListAdapter();
        itemListView.setAdapter(mAdapter);

        //Builds RecyclerView with SelectionTracker
        selectionTracker = new SelectionTracker.Builder<>(
                "my-selection-id",
                itemListView,
                new MyItemKeyProvider(1, mAdapter.deviceList),
                new MyItemLookup(itemListView),
                StorageStrategy.createLongStorage()
        )
                .withOnItemActivatedListener(new OnItemActivatedListener<Long>() {
                    @Override
                    public boolean onItemActivated(@androidx.annotation.NonNull
                                                           ItemDetailsLookup.ItemDetails<Long> item,
                                                   @androidx.annotation.NonNull MotionEvent e) {
                        Log.d(TAG, "Selected Device: " + item.toString());
                        return(true);
                    }
                })
                .build();
        mAdapter.setSelectionTracker(selectionTracker);
        selectionTracker.addObserver(new SelectionTracker.SelectionObserver() {
            @Override
            public void onItemStateChanged(@androidx.annotation.NonNull Object key, boolean selected) {
                super.onItemStateChanged(key, selected);
            }
            @Override
            public void onSelectionRefresh() {super.onSelectionRefresh();}
            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
                Log.d(TAG, "----- selectionTracker" + selectionTracker.hasSelection() );
                Log.d(TAG, "actionMode is null " + (actionMode==null) );

                if(selectionTracker.hasSelection() && actionMode==null) {
                    invalidateOptionsMenu();
                    actionMode = startSupportActionMode(
                            new ActionModeController(MainActivity.this, selectionTracker));
                    invalidateOptionsMenu();
                } else if (!selectionTracker.hasSelection() && actionMode != null) {
                    actionMode.finish();
                    actionMode = null;
                } else {
                    invalidateOptionsMenu();
                }

                //Display devices selected in Logcat
                Iterator<Item> itemIterable = selectionTracker.getSelection().iterator();
                Log.d(TAG, "Device is Empty: " +
                        selectionTracker.getSelection().isEmpty() +
                        selectionTracker.getSelection().size());
                while (itemIterable.hasNext()) {
                    Log.i(TAG, itemIterable.next().getBluetoothAddress());
                }
            }
            @Override
            public void onSelectionRestored() {super.onSelectionRestored();}
        });
        if(savedInstanceState!= null) {
            selectionTracker.onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu Created");
        getMenuInflater().inflate(R.menu.main_menu, menu);

        //Save menu for later use
        menuPrivate = menu;

        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
        }

        if (selectionTracker.hasSelection()) {
            menu.findItem(R.id.menu_connect_devices).setVisible(true);
            menu.findItem(R.id.menu_num).setVisible(true);
            menu.findItem(R.id.menu_num)
                    .setTitle(""+selectionTracker.getSelection().size());
        } else {
            menu.findItem(R.id.menu_connect_devices).setVisible(false);
            menu.findItem(R.id.menu_num).setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mAdapter.clear();
                selectionTracker.clearSelection();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
            case R.id.menu_connect_devices:
                connect_devices(selectionTracker.getSelection());
                break;
            case R.id.menu_num:
                selectionTracker.clearSelection();
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume started");
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        mAdapter.clear();
        scanLeDevice(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersitentState){
        super.onSaveInstanceState(outState, outPersitentState);
        selectionTracker.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.clear();

        final BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mScanning) {
            bluetoothLeScanner.stopScan(mLeScanCallback);
            mScanning = false;
        }
    }

/**
 *  Self Made Functions:
 *   - scanLeDevice
 *   - connect_devices
 *   - mLeScanCallback
 **/
 private void connect_devices (Selection<Item> connectable_items) {
        //Start Activity that controls read/write
        final BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        final ArrayList<String> deviceNames = new ArrayList<String>();
        final ArrayList<String> deviceAddresses = new ArrayList<String>();
        BluetoothDevice tempDevice=null;

        for (Item temp_item: connectable_items) {
            String temp_address = temp_item.getBluetoothAddress();
            tempDevice = mBluetoothAdapter.getRemoteDevice(temp_address);
            deviceNames.add(tempDevice.getName());
            deviceAddresses.add(tempDevice.getAddress());
            Log.d(TAG, tempDevice.getAddress());
        }
        Log.d(TAG, "Size of Connectable Device List is: " + deviceAddresses.size());

        final Intent intent = new Intent(MainActivity.this, DeviceControlActivity.class);
        intent.putStringArrayListExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, deviceNames);
        intent.putStringArrayListExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, deviceAddresses);

        if (mScanning) {
            bluetoothLeScanner.stopScan(mLeScanCallback);
            mScanning = false;
        }

        selectionTracker.clearSelection();

        startActivity(intent);
    }

    private void scanLeDevice(final boolean enable) {
        Log.d(TAG, "scanLeDevice started");
        final BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        Log.d(TAG, "boolean enable: " + Boolean.toString(enable));
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mExit){
                        mScanning = false;
                        bluetoothLeScanner.stopScan(mLeScanCallback);
                        invalidateOptionsMenu();
                    }
                    mExit = true;
                }
            }, SCAN_PERIOD);

            mScanning = true;
            Log.d(TAG, "Scan is Started via startScan");
            bluetoothLeScanner.startScan(mLeScanCallback);
        } else {
            mScanning = false;
            mExit = false;
            bluetoothLeScanner.stopScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    // Device scan callback.
    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (result.getDevice().getName() != null) {
                mAdapter.add(mBluetoothAdapter.getRemoteDevice(result.getDevice().toString()));
            }
        }
        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG, "Scan Failed");
            Log.d(TAG, Integer.toString(errorCode));
            super.onScanFailed(errorCode);
        }
    };
}
