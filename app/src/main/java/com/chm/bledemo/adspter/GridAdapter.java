package com.chm.bledemo.adspter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.chm.bledemo.R;
import com.hjy.bluetooth.entity.BluetoothDevice;

import java.util.List;

public class GridAdapter extends BaseAdapter {

    private Context mContext;

    private List<BluetoothDevice> bluetoothDeviceList;

    private List<String> list;

    public GridAdapter(Context mContext, List<BluetoothDevice> bluetoothDeviceList, List<String> list) {
        this.mContext = mContext;
        this.bluetoothDeviceList = bluetoothDeviceList;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        GridViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.grid_list_item,null);
            holder = new GridViewHolder();
            holder.imageView = view.findViewById(R.id.grid_item_iv);
            holder.mTexTView = view.findViewById(R.id.grid_item_tv);
            view.setTag(holder);
        } else {
            holder=(GridViewHolder) view.getTag();
        }

        holder.mTexTView.setText(list.get(position));
        holder.imageView.setBackgroundResource(R.drawable.ic_bluetooth);

        return view;
    }
}
