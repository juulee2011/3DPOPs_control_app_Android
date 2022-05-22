package com.optogenetic_control.wpt_ble1;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.AsyncTask;
import android.util.Log;

import java.util.UUID;

public class Operation extends AsyncTask<BluetoothGatt, Integer, String> {
    String bluetoothAddressOperation;
    String operationType;
    String value;
    BluetoothGatt deviceGatt;
    BluetoothGattCharacteristic gattCharacteristic;
    BluetoothLeService BLEService;
    BluetoothGattDescriptor deviceDescriptor;

    private final static String TAG = Operation.class.getSimpleName();

    private final static String READ_CHARACTERISTIC="2d30c082-f39f-4ce6-923f-3484ea480596";
    private final static String WRITE_CHARACTERISTIC="2d30c083-f39f-4ce6-923f-3484ea480596";
    private final static String SERVICE_UUID="0000fe84-0000-1000-8000-00805f9b34fb";
    private UUID DESCRIPTOR_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    Operation(String bluetoothAddressOperation , String value, String operationType, BluetoothLeService BLEService){
        this.bluetoothAddressOperation = bluetoothAddressOperation;
        this.operationType = operationType;
        this.value = value;
        this.BLEService = BLEService;
    }

    //Pass Params: BluetoothGatt for connection to each device
    @Override
    protected String doInBackground(BluetoothGatt... strings) {
        Log.d(TAG, "Params for AsyncTask: "+ strings[0]);
        deviceGatt = strings[0];
        runOperation();
        return null;
    }

    @Override
    protected void onPreExecute() { }

    @Override
    protected void onProgressUpdate(Integer... values) { }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "AsyncTask is done");
    }

    public void runOperation(){
        switch(operationType) {
            case "DISCONNECT":
                writeDeviceB();
                disconnectDescriptor();
                disconnectDevice();
                break;
            case "WRITE":
                Log.d(TAG, "OPERATION WRITE starting");
                writeDevice();
                break;
            case "RESET":
                Log.d(TAG, "OPERATION RESET starting");
                disconnectDescriptor();
                writeDeviceR();
                disconnectDevice();
                break;
            case "CONNECT":
                Log.d(TAG, "OPERATION CHARACTERISTIC_SET starting");
                connectDescriptor();
                writeDevice();
                break;
        }
    }
    private void writeDeviceR(){
        Log.d(TAG, "writeDeviceR: " + deviceGatt.getDevice().getAddress());
        gattCharacteristic = deviceGatt
                .getService(UUID.fromString(SERVICE_UUID))
                .getCharacteristic(UUID.fromString(WRITE_CHARACTERISTIC));

        gattCharacteristic.setValue("r");
        deviceGatt.writeCharacteristic(gattCharacteristic);
    }

    private void writeDeviceB(){
        gattCharacteristic = deviceGatt
                .getService(UUID.fromString(SERVICE_UUID))
                .getCharacteristic(UUID.fromString(WRITE_CHARACTERISTIC));

        gattCharacteristic.setValue("B");
        deviceGatt.writeCharacteristic(gattCharacteristic);
    }

    private void writeDevice(){
        gattCharacteristic = deviceGatt
                .getService(UUID.fromString(SERVICE_UUID))
                .getCharacteristic(UUID.fromString(WRITE_CHARACTERISTIC));

        gattCharacteristic.setValue(value);
        deviceGatt.writeCharacteristic(gattCharacteristic);
    }
    private void disconnectDevice(){
        deviceGatt.disconnect();
        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            System.out.println("got interrupted!");
        }
    }
    private void disconnectDescriptor(){
        gattCharacteristic = deviceGatt
                .getService(UUID.fromString(SERVICE_UUID))
                .getCharacteristic(UUID.fromString(READ_CHARACTERISTIC));

        // Write on the config descriptor to be notified when the value changes
        deviceGatt.setCharacteristicNotification(gattCharacteristic, false);
        deviceDescriptor =
                gattCharacteristic.getDescriptor(DESCRIPTOR_CONFIG_UUID);
        deviceDescriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);

        deviceGatt.writeDescriptor(deviceDescriptor);
        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            System.out.println("got interrupted!");
        }
    }
    private void connectDescriptor(){
        Log.d(TAG, "deviceGatt is:" + Boolean.toString(deviceGatt==null));
        gattCharacteristic = deviceGatt
                .getService(UUID.fromString(SERVICE_UUID))
                .getCharacteristic(UUID.fromString(READ_CHARACTERISTIC));

        // Enable notifications for this characteristic locally
        deviceGatt.setCharacteristicNotification(gattCharacteristic, true);

        // Write on the config descriptor to be notified when the value changes
        deviceDescriptor =
                gattCharacteristic.getDescriptor(DESCRIPTOR_CONFIG_UUID);
        deviceDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        deviceGatt.writeDescriptor(deviceDescriptor);

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            System.out.println("got interrupted!");
        }
    }
}