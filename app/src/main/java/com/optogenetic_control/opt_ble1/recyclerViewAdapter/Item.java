package com.optogenetic_control.wpt_ble1.recyclerViewAdapter;

public class Item {
    private String deviceName;
    private String bluetoothAddress;

    public Item(String a, String b){
        this.deviceName = a;
        this.bluetoothAddress = b;
    }

    //Return Item fields
    public String getDevicename() {return this.deviceName;}
    public String getBluetoothAddress() {return this.bluetoothAddress;}

}
