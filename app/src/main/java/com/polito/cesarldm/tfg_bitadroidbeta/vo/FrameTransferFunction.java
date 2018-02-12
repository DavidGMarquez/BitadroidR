package com.polito.cesarldm.tfg_bitadroidbeta.vo;

import com.github.mikephil.charting.data.Entry;

import java.math.BigDecimal;

import info.plux.pluxapi.bitalino.BITalinoFrame;

/**
 * Created by CesarLdM on 12/7/17.
 */

public class FrameTransferFunction {
    private ChannelConfiguration mChannelConfig;
    private static float twoToTheNOne=1024;
    private static float twoToTheNTwo=64;
    private static double vcc= 3.3;
    private float n;


    public FrameTransferFunction(ChannelConfiguration mChannelConfig){
        this.mChannelConfig=mChannelConfig;

    }

    public float[] getConvertedValues(BITalinoFrame frame){
        float[] convertedValues=new float[6];
        for (int i = 0; i < mChannelConfig.activeChannels.length; i++){
            if(i>4){
                n=twoToTheNTwo;
            }else{
                n=twoToTheNOne;
            }
            float f=(float)frame.getAnalog(mChannelConfig.activeChannels[i]);
            switch (mChannelConfig.activeChannelsNames[i]){
                case "EMG":
                    float temp1= (float) ((f/n)-0.5);
                    temp1= (float) (temp1*vcc*1000);
                    temp1=temp1/1009;
                    temp1=round(temp1,2);
                    convertedValues[i]=temp1;
                    break;
                case "ECG":
                    float temp2= (float) ((f/n)-0.5);
                    temp2= (float) (temp2*vcc*1000);
                    temp2=temp2/1100;
                    temp2=round(temp2,2);
                    convertedValues[i]=temp2;
                    break;
                case "EDA":
                    float temp3=(f/n);
                    temp3= (float) (temp3*vcc);
                    temp3= (float) (temp3-0.574);
                    temp3= (float) (temp3/0.132);
                    temp3=round(temp3,2);
                    convertedValues[i]=temp3;
                    break;
                case "EEG":
                    float temp4= (float) ((f/n)-0.5);
                    temp4= (float) (temp4*vcc);
                    temp4=temp4/40000;
                    temp4=round(temp4,2);
                    convertedValues[i]=temp4;
                    break;
                case "ACC":
                    float temp5=f-208;
                    temp5=(temp5/104);
                    temp5=temp5*2;
                    temp5=temp5-1;
                    temp5=round(temp5,2);
                    convertedValues[i]=temp5;
                    break;
                case "LUX":
                    float temp6=f/n;
                    temp6=temp6*100;
                    temp6=round(temp6,2);
                    convertedValues[i]=temp6;
                    break;
                default:
                    convertedValues[i]=f;
            }

        }
        return convertedValues;
    }
    private static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

}
