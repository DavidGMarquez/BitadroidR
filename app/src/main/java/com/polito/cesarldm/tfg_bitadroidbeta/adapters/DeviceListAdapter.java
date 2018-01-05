package com.polito.cesarldm.tfg_bitadroidbeta.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.polito.cesarldm.tfg_bitadroidbeta.R;

import java.util.ArrayList;

import static android.R.drawable.stat_sys_data_bluetooth;

/**
 * Created by CesarLdM on 19/5/17.
 */

public class DeviceListAdapter extends BaseAdapter{

    Context context;
    private ArrayList<BluetoothDevice> devices;

    public DeviceListAdapter(Context context){
        super();
        this.devices=new ArrayList<>();
        this.context=context;

    }
    public void addDevice(BluetoothDevice device) {
        if(!devices.contains(device)) {
            devices.add(device);
        }
    }
    public void clear(){devices.clear();}
    public void setDeviceArray(ArrayList<BluetoothDevice> bitalinoDevices){this.devices=bitalinoDevices;}
    @Override
    public int getCount() {return devices.size();}
    @Override
    public BluetoothDevice getItem(int position) {return devices.get(position);}
    @Override
    public long getItemId(int position) {return position;}
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView==null){
            LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView=inflater.inflate(R.layout.bth_list_item, null);
        }
        //Otherwise
        holder=new ViewHolder();
        //initialize views
        holder.deviceName= (TextView) convertView.findViewById(R.id.tv_BDLI_name);
        holder.deviceAddress=(TextView) convertView.findViewById(R.id.tv_BDLI_address);
        holder.imageView=(ImageView)convertView.findViewById(R.id.iv_BDLI_image);
        //assign views data
        holder.deviceName.setText(devices.get(position).getName());
        holder.deviceAddress.setText(devices.get(position).getAddress());
        holder.imageView.setImageResource(stat_sys_data_bluetooth);

        //add a CONNECTED/DISCONECTED INDICATOR
        return convertView;
    }
    static class ViewHolder{
        TextView deviceName;
        TextView deviceAddress;
        ImageView imageView;
    }
    //... Métodos
}
