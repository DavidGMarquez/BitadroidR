package com.polito.cesarldm.tfg_bitadroidbeta.vo;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Switch;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static android.content.ComponentName.readFromParcel;

/**
 * Created by CesarLdM on 20/5/17.
 */

public class ChannelConfiguration implements Parcelable{

    public String name;
    public int[] activeChannels;
    public String[] activeChannelsNames;
    public int sampleRate;
    public int visualizationRate;
    public String creationDate;
    private static DateFormat df=new SimpleDateFormat("dd/mm/yyyy");
    public int[] recordingChannels;
    public String[] shownNames;


    public ChannelConfiguration(int[] activeChannels){
        this.activeChannels=activeChannels;
        this.sampleRate=10;
    }


    public ChannelConfiguration(String name,int[] activeChannels,int[]recordingChannels,int sampleRate,String[] activeChannelsNames,String[] shownNames){
        this.activeChannels=activeChannels;
        this.sampleRate=sampleRate;
        this.name=name;
        this.activeChannelsNames=activeChannelsNames;
        this.recordingChannels=recordingChannels;
        this.shownNames=shownNames;
        switch (sampleRate){
            case 1:
                this.visualizationRate=1;
                break;
            case 10:
                this.visualizationRate=10;
                break;
            case 100:
                this.visualizationRate=100;
                break;
            case 1000:
                this.visualizationRate=100;
                break;
            default:
                this.visualizationRate=100;
        }
        Date now=Calendar.getInstance().getTime();
        this.creationDate=df.format(now);

    }

    public int[] getRecordingChannels() {
        return recordingChannels;
    }

    public void setRecordingChannels(int[] recordingChannels) {
        this.recordingChannels = recordingChannels;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
    public int getVisualizationRate() {
        return visualizationRate;
    }

    public void setVisulizationRate(int visulizationRate) {
        this.visualizationRate = visulizationRate;
    }

    public String getName() {
        return name;
    }
    public int getSize(){
        return recordingChannels.length;
    }

    public String channelsToString(){
        String channelString= Arrays.toString(activeChannels);

        return channelString;
    }

    public String[] getActiveChannelsNames() {
        return activeChannelsNames;
    }

    public void setActiveChannelsNames(String[] activeChannelsNames) {
        this.activeChannelsNames = activeChannelsNames;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int[] getActiveChannels() {
        return activeChannels;
    }

    public void setActiveChannels(int[] activeChannels) {
        this.activeChannels = activeChannels;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getActiveChannelListSize(){

        return activeChannels.length;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeIntArray(activeChannels);
        dest.writeInt(sampleRate);
        dest.writeInt(visualizationRate);
        dest.writeString(creationDate);
        dest.writeStringArray(activeChannelsNames);
        dest.writeIntArray(recordingChannels);
        dest.writeStringArray(shownNames);

    }
    public static final Parcelable.Creator<ChannelConfiguration> CREATOR
            =new Parcelable.Creator<ChannelConfiguration>(){

        public ChannelConfiguration createFromParcel(Parcel in){
            return new ChannelConfiguration(in);
        }
    public ChannelConfiguration[] newArray(int size){
        return new ChannelConfiguration[size];

    }

    };

    private ChannelConfiguration(Parcel in){
        name=in.readString();
        activeChannels=in.createIntArray();
        sampleRate=in.readInt();
        visualizationRate=in.readInt();
        creationDate=in.readString();
        activeChannelsNames=in.createStringArray();
        recordingChannels=in.createIntArray();
       shownNames=in.createStringArray();

    }

}
