package com.optogenetic_control.wpt_ble1.recyclerViewAdapter;

import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;

public class MyItemDetail extends ItemDetailsLookup.ItemDetails {
    private final int adapaterPosition;
    private final Item selectionKey;

    public MyItemDetail(int adapterPosition, Item selectionKey){
        this.adapaterPosition = adapterPosition;
        this.selectionKey = selectionKey;
    }

    @Override
    public int getPosition(){return adapaterPosition;}

    @Nullable
    @Override
    public Object getSelectionKey(){return selectionKey;}
}
