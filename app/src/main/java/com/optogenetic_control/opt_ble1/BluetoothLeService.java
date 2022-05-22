package com.optogenetic_control.wpt_ble1;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_DISCONNECTED2 =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED2";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static String EXTRA_DATA_ID =
            "com.example.bluetooth.le.EXTRA_DATA_ID";

    public static ArrayList<String> connectedDevices = new ArrayList<String>();
    private static ArrayList<BluetoothGatt> bluetoothGatts = new ArrayList<BluetoothGatt>();
    static Queue<Operation> operationQueue = new LinkedList<Operation>();
    private static Operation currentOperation;
    BluetoothGatt mBluetoothGatt1;

    public void loop() {
        currentOperation = operationQueue.poll();

        if (currentOperation == null) {
            Log.d("DebugInfo", "Queue is Empty");
            return;
        }

        Log.d("Debug", "-------- New Loop Started---------");
        Log.d("Debug", "value:" + currentOperation.value);
        Log.d("Debug", "Operation Type:" + currentOperation.operationType);
        Log.d("Debug", "Operation Address:" + currentOperation.bluetoothAddressOperation);
        if (connectedDevices.contains(currentOperation.bluetoothAddressOperation)) {
            //already connected so run operation
            Log.d("Debug", "Already Connected, Run");
            BluetoothGatt tempGatt = bluetoothGatts.get(connectedDevices.indexOf(currentOperation.bluetoothAddressOperation));
            currentOperation.execute(tempGatt);
        } else {
            Log.d("Debug", "Connect and Run");
            connect(currentOperation.bluetoothAddressOperation);
        }
    }

    public Boolean queue(Operation addOperation){
        //Add to operation and connected devices queue
        operationQueue.add(addOperation);
        return true;
    }

    public void start(){
        //Add to operation and connected devices queue
        loop();
        return;
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic,
                                 final String mAddress) {
        byte[] data = null;
        final Intent intent = new Intent(action);

        if (characteristic != null) {
            Log.d("Data Received", Arrays.toString(data));

            data = characteristic.getValue();
            intent.putExtra(EXTRA_DATA, new String(data)); }

        Log.d("Debug", "Broadcast Update");

        intent.putExtra(EXTRA_DATA_ID, mAddress);
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    public boolean connect(final String address) {
        final BluetoothDevice device1 = mBluetoothAdapter.getRemoteDevice(address);
        BluetoothGattCallback mGattCallback1 = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // Attempts to discover services after successful connection.
                    Log.i(TAG, "Attempting to start service discovery");
                    String intentAction = ACTION_GATT_CONNECTED;
                    broadcastUpdate(intentAction, null, device1.getAddress());
                    connectedDevices.add(address);
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "Disconnected from GATT server.");
                    String intentAction = ACTION_GATT_DISCONNECTED;
                    connectedDevices.remove(device1.getAddress());
                    bluetoothGatts.remove(gatt);
                    broadcastUpdate(intentAction, null, device1.getAddress());
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "BLE Services Discovered");
                    String intentAction = ACTION_GATT_SERVICES_DISCOVERED;
                    broadcastUpdate(intentAction, null, gatt.getDevice().getAddress());
                } else {
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt,
                                                BluetoothGattCharacteristic characteristic) {
                if ((char) characteristic.getValue()[0] != 'X') {
                    Log.d("DebugInfo", "Characteristic Changed");
                    Log.d("Character Changed to", (char) characteristic.getValue()[0] + "");
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic, gatt.getDevice().getAddress());
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt,
                                              BluetoothGattCharacteristic characteristic,
                                              int status) {
                Log.d("DebugInfo", "Characteristic Write Executed");
                loop();
            }
        };

        mBluetoothGatt1 = device1.connectGatt(this, false, mGattCallback1);

        bluetoothGatts.add(mBluetoothGatt1);
        return true;
    }

    public void disconnectAll() {
        Log.d("Debug", "Disconnecting " + connectedDevices.size());
        bluetoothGatts.clear();
        connectedDevices.clear();
    }
}