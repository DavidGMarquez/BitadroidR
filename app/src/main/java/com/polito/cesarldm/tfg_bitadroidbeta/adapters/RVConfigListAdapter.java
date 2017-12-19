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

import java.util.ArrayList;

/**
 * Created by CesarLdM on 1/9/17.
 */

public class RVConfigListAdapter extends RecyclerView.Adapter<RVConfigListAdapter.ConfigViewHolder> {
    Context ctxt;
    ArrayList<ChannelConfiguration>configs=new ArrayList<ChannelConfiguration>();


    public static class ConfigViewHolder extends RecyclerView.ViewHolder{

        CardView cv;
        ImageView  ivcard,ivSampled,ivShown;
        TextView tvName,tvSampleRate,tvShownRate,tvSampledChannels,tvShownChannels,tvDate;

        public ConfigViewHolder(View itemView) {
            super(itemView);
            cv=(CardView)itemView.findViewById(R.id.cv);
            ivcard=(ImageView)itemView.findViewById(R.id.iv_card_image);
            ivSampled=(ImageView)itemView.findViewById(R.id.iv_sampled);
            ivShown=(ImageView)itemView.findViewById(R.id.iv_shown);
            tvName=(TextView)itemView.findViewById(R.id.name_conf_card);
            tvSampleRate=(TextView)itemView.findViewById(R.id.tv_sampled);
            tvShownRate=(TextView)itemView.findViewById(R.id.tv_shown);
            tvSampledChannels=(TextView)itemView.findViewById(R.id.tv_channels_sampled);
            tvShownChannels=(TextView)itemView.findViewById(R.id.tv_channels_shown);
            tvDate=(TextView)itemView.findViewById(R.id.tv_date);
        }
    }
    public RVConfigListAdapter(ArrayList<ChannelConfiguration>configs){
        this.configs=configs;
    }

    public RVConfigListAdapter(Context context){
        this.ctxt=context;

    }
    public void addConfiguration(ChannelConfiguration channelConfiguration){
        this.configs.add(channelConfiguration);
    }
    public void setArray(ArrayList<ChannelConfiguration>configs){
        this.configs=configs;
    }

    @Override
    public ConfigViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(this.ctxt).inflate(R.layout.config_card_bis,parent,false);
        ConfigViewHolder cvh=new ConfigViewHolder(v);
        return cvh;
    }

    @Override
    public void onBindViewHolder(ConfigViewHolder holder, int position) {
        switch(configs.get(position).getActiveChannelListSize()){
            case 0:
                holder.ivcard.setImageResource(R.drawable.ic_filter_none_black_24dp);
                break;
            case 1:
                holder.ivcard.setImageResource(R.drawable.ic_1_channel);
                break;
            case 2:
                holder.ivcard.setImageResource(R.drawable.ic_filter_2_black_24dp);
                break;
            case 3:
                holder.ivcard.setImageResource(R.drawable.ic_filter_3_black_24dp);
                break;
            case 4:
                holder.ivcard.setImageResource(R.drawable.ic_filter_4_black_24dp);
                break;
            case 5:
                holder.ivcard.setImageResource(R.drawable.ic_filter_5_black_24dp);
                break;
            case 6:
                holder.ivcard.setImageResource(R.drawable.ic_filter_6_black_24dp);
                break;
            default:
                holder.ivcard.setImageResource(R.drawable.ic_filter_none_black_24dp);
                break;
        }
        holder.tvName.setText(configs.get(position).getName());
        holder.tvSampleRate.setText(configs.get(position).getSampleRate()+"Hz");
        holder.tvShownRate.setText(configs.get(position).getVisualizationRate()+"Hz");
        holder.tvSampledChannels.setText(configs.get(position).channelsToString());
        holder.tvShownChannels.setText(configs.get(position).shownToString());
        holder.tvDate.setText(configs.get(position).getCreationDate());
    }

    @Override
    public int getItemCount() {
        return configs==null? 0:configs.size();
    }
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView){
        super.onAttachedToRecyclerView(recyclerView);
    }
    public ChannelConfiguration getSelectedCC(int position){
        return configs.get(position);
    }
    public void deleteConfig(int position) {
        configs.remove(position);
        this.notifyItemRemoved(position);
    }
    public void updateConfiguration(int longPressedPosition, ChannelConfiguration mChannelConfig) {
        configs.remove(longPressedPosition);
        configs.add(longPressedPosition,mChannelConfig);
        this.notifyDataSetChanged();
    }
}
