package com.optogenetic_control.wpt_ble1.recyclerViewAdapter;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.optogenetic_control.wpt_ble1.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ItemListViewHolder> {
    public List<Item>deviceList;
    private final List<BluetoothDevice>bluetoothDeviceList;
    private SelectionTracker selectionTracker;
    Item temp;

    public ItemListAdapter(){
        super();
        deviceList = new ArrayList<Item>();
        bluetoothDeviceList = new ArrayList<BluetoothDevice>();
    }

    public void add(BluetoothDevice new_device){
        if(!bluetoothDeviceList.contains(new_device)) {
            if (new_device.getName()==null){temp = new Item("*Unknown*",new_device.getAddress());}
            else{temp = new Item(new_device.getName(),new_device.getAddress());}
            deviceList.add(temp);
            bluetoothDeviceList.add(new_device);
            notifyDataSetChanged();}
    }

    public void clear() {
        deviceList.clear();
        bluetoothDeviceList.clear();}

    public class ItemListViewHolder extends RecyclerView.ViewHolder implements ViewHolderWithDetails{
        TextView itemDeviceName, itemBluetoothAddress;

        public ItemListViewHolder(@NonNull View itemView){
            super(itemView);
            itemDeviceName =itemView.findViewById(R.id.device_name);
            itemBluetoothAddress =itemView.findViewById(R.id.device_address);
        }

        public final void bind(Item item, boolean isActive){
            itemView.setActivated(isActive);
            itemDeviceName.setText(item.getDevicename());
            itemBluetoothAddress.setText(item.getBluetoothAddress());
        }

        @Override
        public ItemDetailsLookup.ItemDetails getItemDetails(){
            return new MyItemDetail(getAdapterPosition(), deviceList.get(getAdapterPosition()));
        }
    }
    public SelectionTracker setSelectionTracker(SelectionTracker tracker){return this.selectionTracker = tracker;}

    @NonNull
    @Override
    public ItemListViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_main, parent, false);
        return new ItemListViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ItemListViewHolder holder, int position ){
        Item item = deviceList.get(position);
        holder.bind(item, selectionTracker.isSelected(item));}
    @Override
    public int getItemCount() {return deviceList.size();}
}