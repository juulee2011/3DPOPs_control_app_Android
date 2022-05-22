package com.optogenetic_control.wpt_ble1;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;

import androidx.appcompat.app.AppCompatActivity;

public class DeviceControlActivity extends AppCompatActivity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private static ArrayList<String> mDeviceNames;
    private static ArrayList<String> mDeviceAddresses;
    private BluetoothLeService mBluetoothLeService1;
    private BluetoothManager mBluetoothManager;
    static customListViewAdapter adapterMy;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection1 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(TAG, "onServiceConnected Started");
            mBluetoothLeService1 = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService1.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            for (String oneAddress : mDeviceAddresses) {
                Operation temp = new Operation(oneAddress, "B", "CONNECT", mBluetoothLeService1);
                mBluetoothLeService1.queue(temp);}
            mBluetoothLeService1.start();
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected Started");
            mBluetoothLeService1 = null;
        }
    };

    // Handles various events fired by the Service.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Action Received: " + intent.getAction());
            final String action = intent.getAction();

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                String tempAddress = intent.getStringExtra(BluetoothLeService.EXTRA_DATA_ID);
                adapterMy.changeState(tempAddress,1);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                String tempAddress = intent.getStringExtra(BluetoothLeService.EXTRA_DATA_ID);
                adapterMy.changeState(tempAddress,0);
                adapterMy.disconnectBattery(tempAddress);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED2.equals(action)) {
                Log.d(TAG, "ACTION_GATT_DISCONNECTED2 received");
                String tempAddress = intent.getStringExtra(BluetoothLeService.EXTRA_DATA_ID);
                adapterMy.changeState(tempAddress,2);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, "ACTION_GATT_SERVICE_DISCOVERED received");
                String tempAddress = intent.getStringExtra(BluetoothLeService.EXTRA_DATA_ID);
                Operation temp = new Operation(tempAddress, "B", "CONNECT", mBluetoothLeService1);
                mBluetoothLeService1.queue(temp);
                mBluetoothLeService1.start();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String tempAddress = intent.getStringExtra(BluetoothLeService.EXTRA_DATA_ID);
                String value = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException | NullPointerException nfe) {
                    value = "XX";
                }
                adapterMy.changeBattery(tempAddress,value);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("DebugInformation: ", "onCreate Device Control Activity started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devicecontrol);

        final Intent intent = getIntent();
        mDeviceNames = intent.getStringArrayListExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddresses = intent.getStringArrayListExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        Intent gattServiceIntent1 = new Intent(this, BluetoothLeService.class);
        boolean test = bindService(gattServiceIntent1, mServiceConnection1, BIND_AUTO_CREATE);
        Log.d("DebugInformation: ", "Service Bind1 was " + Boolean.toString(test));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("DebugInformation: ", "onResume Device Control Activity started");
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        Log.d("DebugInfo", "Service 1 Connected Resume: " + Boolean.toString(mBluetoothLeService1!=null));

        ListView lv = findViewById(R.id.listViewDevices);
        adapterMy = new customListViewAdapter(this,
                mDeviceNames,
                mDeviceAddresses,
                new ArrayList<String>(Collections.nCopies(7, "Disconnected")),
                new ArrayList<String>(Collections.nCopies(7, "0%"))
        );
        lv.setAdapter(adapterMy);
        lv.setDivider(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection1);
        mBluetoothLeService1 = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.devicecontrol_menu, menu);
        menu.findItem(R.id.menu_send).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_clearitems:
                adapterMy.clearSelection();
                break;
            case R.id.menu_send:
                sendCommands();
                break;
            case R.id.menu_disconnect:
                disconnectAll();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void sendCommands() {
        ArrayList<String> connectedDevices = mDeviceAddresses;
        ArrayList<Character> commands1 = adapterMy.getStates1();
        ArrayList<Character> commands2 = adapterMy.getStates2();
        Operation temp;

        for(int j=0;  j<connectedDevices.size(); j++) {
            String connectedDevice = connectedDevices.get(j);
            if(commands1.get(j)== 'r') {
                Log.d(TAG, "sendCommands RESET: " + connectedDevice);
                temp = new Operation(connectedDevice, Character.toString(commands1.get(j)), "RESET", mBluetoothLeService1);}
            else if (commands1.get(j)== 'x') continue;
            else {temp = new Operation(connectedDevice, Character.toString(commands1.get(j)), "WRITE", mBluetoothLeService1);}

            mBluetoothLeService1.queue(temp);
        }

        if(mBluetoothLeService1.connectedDevices.size()>0) {
            for (int j = 0; j < connectedDevices.size(); j++) {
                String connectedDevice = connectedDevices.get(j);
                if (commands2.get(j) == 'r') {
                    Log.d(TAG, "sendCommands RESET: " + connectedDevice);
                    temp = new Operation(connectedDevice, Character.toString(commands2.get(j)), "RESET", mBluetoothLeService1);
                } else if (commands2.get(j) == 'x') continue;
                else {
                    temp = new Operation(connectedDevice, Character.toString(commands2.get(j)), "WRITE", mBluetoothLeService1);
                }
                mBluetoothLeService1.queue(temp);
            }
        }
        mBluetoothLeService1.start();
    }

    public void disconnectAll() {
        for (String oneAddress : mDeviceAddresses) {
            Operation temp = new Operation(oneAddress, "B", "DISCONNECT", mBluetoothLeService1);
            mBluetoothLeService1.queue(temp);
            mBluetoothLeService1.start();
        }
        mBluetoothLeService1.disconnectAll();
    }
}
