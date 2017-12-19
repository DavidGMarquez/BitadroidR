package com.polito.cesarldm.tfg_bitadroidbeta.vo;

import java.math.BigDecimal;

import info.plux.pluxapi.bitalino.BITalinoFrame;

/**
 * Created by CesarLdM on 8/8/17.
 */

public class SignalFilter {

    private float yMax,yMin,avg, sumForAvg;
    private float upThreshold,downThreshold;
    private Linechart linechart;

    public SignalFilter(Linechart linechart){
        this.linechart=linechart;
        this.upThreshold=0;
        this.downThreshold=1;
        sumForAvg=0;
    }
    public void updateValues(float sumForAvg){
        float size=linechart.getDataSetSize();
        yMax=linechart.getYMax();
        yMin=linechart.getYMin();
        avg=sumForAvg/size;
        avg=round(avg,2);


    }
    private static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }


    public float getyMax() {
        return yMax;
    }

    public void setyMax(float yMax) {
        this.yMax = yMax;
    }

    public float getyMin() {
        return yMin;
    }

    public void setyMin(float yMin) {
        this.yMin = yMin;
    }

    public float getAvg() {
        return avg;
    }

    public void setAvg(float avg) {
        this.avg = avg;
    }

    public float getUpThreshold() {
        return upThreshold;
    }

    public void setUpThreshold(int upThreshold) {

        this.upThreshold = upThreshold/100f;
        this.downThreshold=upThreshold/100f;
        this.downThreshold=1-downThreshold;
    }

    public float getDownThreshold() {
        return downThreshold;
    }

    public void setDownThreshold(int downThreshold) {

        this.downThreshold = downThreshold/100;
        this.downThreshold=1-downThreshold;
    }

    public Linechart getLinechart() {
        return linechart;
    }

    public void setLinechart(Linechart linechart) {
        this.linechart = linechart;
    }

    public boolean checkFrame(float value){
        boolean decision;
        if(isAboveAVG(value)){
            decision=isAboveHighThreshold(value);
        }else if(!isAboveAVG(value)){
            decision=isBelowLowThreshold(value);
        }else decision=true;
        return decision;
    }

    private boolean isBelowLowThreshold(float value) {
        float tempValue=value-yMin;
        float tempReference=avg-yMin;
        tempReference=tempReference*downThreshold;
        return tempValue <= tempReference;
    }

    private boolean isAboveHighThreshold(float value) {
        float tempValue=value-this.avg;
        float tempReference=yMax-avg;
        tempReference=tempReference*upThreshold;
        return tempValue >= tempReference;

    }

    private boolean isAboveAVG(float value){
        return value >= this.avg;

    }


}
