package com.polito.cesarldm.tfg_bitadroidbeta.vo;

/**
 * Created by CesarLdM on 30/8/17.
 */

public class LowPassFilter {
    float alpha;

    public LowPassFilter(){
        this.alpha=0.0f;
    }
    public float lowPass(float newValue,float oldValue){
        if(oldValue==0.0f)return  newValue;
        if(alpha==0.0f)return newValue;
        oldValue=oldValue+alpha*(newValue-oldValue);
        return oldValue;
    }

    public void updateAlpha(int value) {
       float threshold=value/100f;
        this.alpha=1-threshold;
    }
}
