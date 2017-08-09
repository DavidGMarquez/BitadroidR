package com.polito.cesarldm.tfg_bitadroidbeta.vo;

import java.util.ArrayList;

import biz.source_code.dsp.math.Complex;
import biz.source_code.dsp.transform.Dft;
import biz.source_code.dsp.util.ArrayUtils;

/**
 * Created by CesarLdM on 25/7/17.
 */

public class DFTManager {
    private Dft dft;
    private ArrayList<Double> doubletoDFT;

    public DFTManager(ArrayList<Double> doubletoDFT){
        this.dft=new Dft();
        this.doubletoDFT=new ArrayList<Double>();
    }
    public DFTManager(){
        this.dft=new Dft();
        this.doubletoDFT=new ArrayList<Double>();
    }
    public void addRealValue(float f){
        double d=f;
        doubletoDFT.add(d);

    }
    public Complex[] getComplexValues(){
        Double[] doubleArrayObject=new Double[doubletoDFT.size()];
       doubletoDFT.toArray(doubleArrayObject);
        double[] doubleArrayPrimitive= ArrayUtils.toDouble(doubleArrayObject);
        Complex[] r=dft.directDft(doubleArrayPrimitive);
        doubletoDFT.clear();
        return r;
    }
}
