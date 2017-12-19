package com.polito.cesarldm.tfg_bitadroidbeta.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.polito.cesarldm.tfg_bitadroidbeta.R;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.ChannelConfiguration;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by CesarLdM on 6/9/17.
 */

public class RVRecordingListAdapter extends RecyclerView.Adapter<RVRecordingListAdapter.RecordingViewHolder> {
    Context ctxt;
    ArrayList<File> files=new ArrayList<File>();



    public static class RecordingViewHolder extends RecyclerView.ViewHolder{

        CardView cv;
        ImageView ivcard,ivSampled,ivShown;
        TextView tvName,tvSampleRate,tvShownRate,tvSampledChannels,tvShownChannels,tvDate;

        public RecordingViewHolder(View itemView) {
            super(itemView);
            cv=(CardView)itemView.findViewById(R.id.cv);
            ivcard=(ImageView)itemView.findViewById(R.id.iv_card_imager);
            ivSampled=(ImageView)itemView.findViewById(R.id.iv_sampledr);
            ivShown=(ImageView)itemView.findViewById(R.id.iv_shownr);
            tvName=(TextView)itemView.findViewById(R.id.name_conf_cardr);
            tvSampleRate=(TextView)itemView.findViewById(R.id.tv_sampledr);
            tvShownRate=(TextView)itemView.findViewById(R.id.tv_shownr);
            tvDate=(TextView)itemView.findViewById(R.id.tv_dater);
        }
    }
    public RVRecordingListAdapter(ArrayList<File> files){
        this.files=files;
    }

    public RVRecordingListAdapter(Context context){
        this.ctxt=context;

    }
    public void addRecording(File file){
        this.files.add(file);
    }

    public void setArray(ArrayList<File> files){
        this.files=files;
    }

    @Override
    public RVRecordingListAdapter.RecordingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(this.ctxt).inflate(R.layout.recording_card_bis,parent,false);
        RVRecordingListAdapter.RecordingViewHolder rvh=new RVRecordingListAdapter.RecordingViewHolder(v);
        return rvh;
    }

    @Override
    public void onBindViewHolder(RVRecordingListAdapter.RecordingViewHolder holder, int position) {
        String superName=files.get(position).getName();
        String[] components=split(superName);

        holder.tvName.setText(components[0]);
        holder.tvSampleRate.setText(components[1]);
        holder.tvShownRate.setText(String.valueOf(files.get(position).length())+" Bytes");
        holder.tvDate.setText(components[2]);
    }

    private String[] split(String superName) {
        String[] tempSArray=superName.split("_");
        return tempSArray;
    }

    @Override
    public int getItemCount() {
        return files==null? 0:files.size();
    }
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView){
        super.onAttachedToRecyclerView(recyclerView);
    }
    public File getSelectedCC(int position){
        return files.get(position);
    }
    public void deleteREC(int position) {
        files.remove(position);
        this.notifyItemRemoved(position);
    }
}
