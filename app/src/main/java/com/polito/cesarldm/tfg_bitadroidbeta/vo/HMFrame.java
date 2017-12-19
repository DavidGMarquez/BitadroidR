package com.polito.cesarldm.tfg_bitadroidbeta.vo;

import android.os.Parcel;
import android.os.Parcelable;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

/**
 * Created by CesarLdM on 10/9/17.
 */

public class HMFrame  {
    private ArrayList<Entry> rrValues=new ArrayList<Entry>();
    private ArrayList<Entry> bpmValues=new ArrayList<Entry>();

    public HMFrame(){

    }
    public ArrayList<Entry> getRrValues() {
        return rrValues;
    }
    public void setRrValues(ArrayList<Entry> rrValues) {
        this.rrValues = rrValues;
    }

    public ArrayList<Entry> getBpmValues() {
        return bpmValues;
    }

    public void setBpmValues(ArrayList<Entry> bpmValues) {
        this.bpmValues = bpmValues;
    }
   public void addRR(Entry rrEntry){
        this.rrValues.add(rrEntry);
    }
    public void addBpm(Entry bpmEntry){
        this.bpmValues.add(bpmEntry);
    }

    @Override
    public String toString(){
        return "HMFrame{" +
                "rrValues=" + rrValues +
                ", bpmValues=" + bpmValues +
                '}';
    }


}

