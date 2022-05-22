package com.optogenetic_control.wpt_ble1.recyclerViewAdapter;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

public class MyItemLookup extends ItemDetailsLookup{
    private final RecyclerView recyclerView;

    public MyItemLookup(RecyclerView recyclerview) {this.recyclerView=recyclerview;}

    @Nullable
    @Override
    public ItemDetails getItemDetails(MotionEvent e) {
        View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(view);
            if (viewHolder instanceof ItemListAdapter.ItemListViewHolder) {
                return ((ItemListAdapter.ItemListViewHolder) viewHolder).getItemDetails();
            }
        }
        return null;
    }
}
