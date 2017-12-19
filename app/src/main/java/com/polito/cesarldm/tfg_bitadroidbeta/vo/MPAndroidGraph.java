package com.polito.cesarldm.tfg_bitadroidbeta.vo;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.polito.cesarldm.tfg_bitadroidbeta.ShowSingleDataActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Created by CesarLdM on 30/6/17.
 */

public class MPAndroidGraph implements OnChartValueSelectedListener, View.OnLongClickListener,View.OnClickListener{
    private  List<Entry> entries;
    private  Context context;
    //private ILineDataSet dataSet;
    private LineChart chart;
   // private LineData lineData;
    private String legendName;
    private int channelNum;
    private ChannelConfiguration mConfiguration;
    private int xRange;
    float lineDataSize;
    float f;
    int zoomValue;
    boolean newZoom=false;
    float xscale;

    private Entry entry;


    public MPAndroidGraph(Context context, ChannelConfiguration mConfiguration, int position){
        this.context=context;
        this.entry=new Entry(0,0);
        this.mConfiguration=mConfiguration;
        this.chart=new LineChart(context);
        this.legendName=mConfiguration.shownNames[position];
        this.channelNum=mConfiguration.recordingChannels[position];
        this.lineDataSize=0;
        chart.setData(new LineData());
        chart.getDescription().setEnabled(true);
        chart.setOnChartValueSelectedListener(this);
        chart.setOnLongClickListener(this);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(true);
        chart.setPinchZoom(true);
        chart.enableScroll();
        chart.setScaleXEnabled(true);
        chart.setScaleYEnabled(false);
        this.f=mConfiguration.getRecordingChannels().length;
        chart.setMinimumHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        chart.setDragOffsetX(30);
        chart.setNoDataText("Press play to start recording");
        chart.setScaleY(1f);
        Legend l = chart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.BLACK);
        XAxis xl = chart.getXAxis();
        xl.setTextColor(Color.BLACK);
        xl.setGranularityEnabled(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setValueFormatter(new MyAxisValueFormatter());
        xl.setGranularity(1f);
        xl.setLabelCount(4);
        xl.setDrawGridLines(true);
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);
        leftAxis.setEnabled(true);
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
        chart.setHardwareAccelerationEnabled(true);
        chart.invalidate();
    }
    public MPAndroidGraph(Context context,String name,int position){
        this.context=context;
        this.entry=new Entry(0,0);
        this.lineDataSize=0;
        this.chart=new LineChart(context);
        this.legendName=name;
        this.channelNum=position;
        chart.setData(new LineData());
        chart.getDescription().setEnabled(true);
        chart.setOnChartValueSelectedListener(this);
        chart.setOnLongClickListener(this);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(true);
        chart.setPinchZoom(true);
        chart.enableScroll();
        chart.setScaleXEnabled(true);
        chart.setScaleYEnabled(false);
        chart.setMinimumHeight(ViewGroup.LayoutParams.MATCH_PARENT);

        chart.setScaleY(1f);
        chart.setNoDataText("Press play to start recording");
        Legend l = chart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.BLACK);
        XAxis xl = chart.getXAxis();
        xl.setTextColor(Color.BLACK);
        xl.setGranularityEnabled(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setValueFormatter(new MyAxisValueFormatter());
        xl.setGranularity(1f);
        xl.setLabelCount(3);
        xl.setDrawGridLines(true);
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);
        leftAxis.setEnabled(true);
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
        chart.setAutoScaleMinMaxEnabled(true);
    }


    public void addEntry(Entry entry){
        LineData lineData= chart.getLineData();
        ILineDataSet dataSet=lineData.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well
            if (dataSet == null) {
                dataSet = createSet();
                lineData.addDataSet(dataSet);
            }
            dataSet.setHighlightEnabled(true);
            lineData.addEntry(entry,0);
            lineData.notifyDataChanged();
            // let the chart know it's data has changed
            chart.notifyDataSetChanged();
            // limit the number of visible entries
            if(newZoom) {
                chart.setVisibleXRangeMaximum(mConfiguration.visualizationRate * zoomValue);
                newZoom=false;
            }
            // move to the latest entry
            lineDataSize++;
        chart.moveViewToX(lineData.getXMax());
        if(chart.getLineData().getDataSetByIndex(0).getEntryCount()==mConfiguration.visualizationRate*10){
            chart.setVisibleXRange(lineDataSize-mConfiguration.getVisualizationRate()*10,lineDataSize);

        }

    }


    private LineDataSet createSet() {
        String color=selectLinecolor();
        LineDataSet set = new LineDataSet(null, this.legendName);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setDrawCircles(false);
        set.setHighlightEnabled(true);
        set.setColor(ColorTemplate.rgb(color));
        set.setLineWidth(1.5f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.rgb(color));
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }


    private String selectLinecolor() {
        String colorCode;
        switch (channelNum){
            case 0:
                colorCode="82D4FF";
            break;
            case 1:
                colorCode="49D317";
            break;
            case 2:
                colorCode="F7910B";
            break;
            case 3:
                colorCode="F7170B";
            break;
            case 4:
                colorCode="F3FF3E";
            break;
            case 5:
                colorCode="A51AD7";
            break;

            default:
                colorCode="82D4FF";
            break;
        }
        return colorCode;
    }

    private void setLineAppearance(LineDataSet set) {
    }

    public void resetZoom(){
    //zoomValue=10;
    chart.fitScreen();
    //newZoom=true;
    }

    public void setChartZoom(int sec){
    zoomValue=sec*100;
    newZoom=true;
    }




    public LineChart getGraphView(){
        return this.chart;
    }
    public float getYMax(){return this.chart.getYMax();}
    public float getYMin(){return this.chart.getYMin();}
    public float getDataSetSize(){
        return lineDataSize;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        this.entry=e;

    }

    @Override
    public void onNothingSelected() {

    }

    public Entry getSelectedValue(){
        return this.entry;}

    public void setHighLightt(float xvalue){
        chart.highlightValue(xvalue,0,true);

    }
    public void saveAsImage(){
        chart.saveToGallery(legendName,50);
    }


    @Override
    public boolean onLongClick(View v) {
        chart.saveToGallery(legendName,80);
        Toast.makeText(this.context,"Snapshot from graph "+this.legendName+", saved",Toast.LENGTH_LONG).show();
        return false;
    }

    @Override
    public void onClick(View v) {

    }
}

