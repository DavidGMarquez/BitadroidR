package com.polito.cesarldm.tfg_bitadroidbeta.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.polito.cesarldm.tfg_bitadroidbeta.R;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.ChannelConfiguration;

import java.util.ArrayList;

/**
 * Created by CesarLdM on 21/5/17.
 */

public class ConfigListAdapter extends BaseAdapter {
    Context context;
    private ArrayList<ChannelConfiguration> configurations;
    //TODO add date to list item


    public ConfigListAdapter(Context context){
        super();
        this.configurations=new ArrayList<ChannelConfiguration>();
        this.context=context;

    }

    public void addConfiguration(ChannelConfiguration config) {
        if(!configurations.contains(config)) {
            configurations.add(config);
            this.notifyDataSetChanged();
        }
    }

    public void clear(){
        configurations.clear();
    }

    public void setConfigArray(ArrayList<ChannelConfiguration> configurations){
        this.configurations=configurations;
    }


    @Override
    public int getCount() {return configurations.size();}

    @Override
    public ChannelConfiguration getItem(int position) {return configurations.get(position);}

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ConfigListAdapter.ViewHolder holder;
        if (convertView==null){
            LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView=inflater.inflate(R.layout.conf_list_item, null);
        }
        //Otherwise
        holder=new ConfigListAdapter.ViewHolder();
        //initialize views
        holder.configName= (TextView) convertView.findViewById(R.id.tv_CLI_name);
        holder.configChannels=(TextView) convertView.findViewById(R.id.tv_CLI_channels);
        holder.configSampleRate=(TextView)convertView.findViewById(R.id.tv_CLI_sampleRate);
        //assign views data
        holder.configName.setText("Name: "+configurations.get(position).getName());
        holder.configChannels.setText("Channels: "+configurations.get(position).channelsToString());
        holder.configSampleRate.setText(String.valueOf(configurations.get(position).getSampleRate())+"Hz");
        //add a CONNECTED/DISCONECTED INDICATOR
        return convertView;
    }



    static class ViewHolder{
        TextView configName;
        TextView configChannels;
        TextView configSampleRate;

    }
}


