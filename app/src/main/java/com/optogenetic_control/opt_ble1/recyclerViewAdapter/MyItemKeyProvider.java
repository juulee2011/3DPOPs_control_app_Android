package com.optogenetic_control.wpt_ble1.recyclerViewAdapter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;

public class MyItemKeyProvider extends ItemKeyProvider {
    private final List<Item> deviceList;

    public MyItemKeyProvider(int scope, List<Item> deviceList){
        super(scope);
        this.deviceList = deviceList;
    }

    @Nullable
    @Override
    public Object getKey(int position){return deviceList.get(position);}

    @Override
    public int getPosition(@NonNull Object key){return deviceList.indexOf(key);}
}
