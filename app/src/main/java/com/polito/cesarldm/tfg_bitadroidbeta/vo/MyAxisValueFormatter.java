package com.polito.cesarldm.tfg_bitadroidbeta.vo;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;

/**
 * Created by CesarLdM on 9/7/17.
 */

public class MyAxisValueFormatter implements IAxisValueFormatter {
   private float xValue;

    public MyAxisValueFormatter(){

    }
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        if (value < 0.000) {
            xValue = 0;
            return "00:00:00";
        }
        xValue = value;
        return String.format("%02d:%02d:%02d", (int) ((value / (1000 * 60 * 60)) % 24), (int) ((value / (1000 * 60)) % 60), (int) (value / 1000) % 60);

    }
}
