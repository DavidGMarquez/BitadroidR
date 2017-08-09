package com.polito.cesarldm.tfg_bitadroidbeta.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.polito.cesarldm.tfg_bitadroidbeta.R;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.ChannelConfiguration;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by CesarLdM on 26/6/17.
 */

public class RecordingListAdapter extends BaseAdapter{
    Context context;
    private ArrayList<File> files;

    public RecordingListAdapter(Context context){
        super();
        this.files=new ArrayList<File>();
        this.context=context;

    }

    public void addFile(File file) {
        if(!files.contains(file)) {
            files.add(file);
            this.notifyDataSetChanged();
        }
    }

    public void clear(){
        files.clear();
    }

    public void setFileArray(ArrayList<File> files){
        this.files=files;
    }


    @Override
    public int getCount() {return files.size();}

    @Override
    public File getItem(int position) {return files.get(position);}

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RecordingListAdapter.ViewHolder holder;
        if (convertView==null){
            LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView=inflater.inflate(R.layout.record_list_item, null);
        }
        //Otherwise
        holder=new RecordingListAdapter.ViewHolder();
        //initialize views
        holder.configName= (TextView) convertView.findViewById(R.id.tv_RLI_name);
        //assign views data
        holder.configName.setText("Name: "+files.get(position).getName());
        //holder.configSampleRate.setText(String.valueOf(files.get(position).getSampleRate())+"Hz");
        //add a CONNECTED/DISCONECTED INDICATOR
        return convertView;
    }



    static class ViewHolder{
        TextView configName;
        TextView configChannels;
        TextView configSampleRate;

    }
}

