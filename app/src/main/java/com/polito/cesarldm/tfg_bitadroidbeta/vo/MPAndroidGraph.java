package com.polito.cesarldm.tfg_bitadroidbeta.vo;
import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;

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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Created by CesarLdM on 30/6/17.
 */

public class MPAndroidGraph implements OnChartValueSelectedListener{
    private  List<Entry> entries;
    private  Context context;
    private ILineDataSet dataSet;
    private LineChart chart;
    private LineData lineData;
    private String legendName;
    private int channelNum;
    private ChannelConfiguration mConfiguration;
    private int xRange;
    float lineDataSize;
    private ViewPortHandler viewPortHandler;
    private Entry entry;


    public MPAndroidGraph(Context context, ChannelConfiguration mConfiguration, int position){
        this.entry=new Entry(0,0);
        this.lineDataSize=0;
        this.mConfiguration=mConfiguration;
        this.chart=new LineChart(context);
        this.legendName=mConfiguration.shownNames[position];
        this.channelNum=mConfiguration.recordingChannels[position];
        this.xRange=mConfiguration.getSampleRate()*10000;
        chart.getDescription().setEnabled(true);
        this.viewPortHandler=chart.getViewPortHandler();
        chart.setOnChartValueSelectedListener(this);
        //setViewPort();

        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(true);
        chart.setPinchZoom(false);
        chart.enableScroll();
        chart.setScaleXEnabled(true);
        chart.setScaleYEnabled(false);
        chart.zoomToCenter(20f,1f);
        chart.setMinimumHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        chart.setDragOffsetX(30);
        this.lineData=new LineData();
        chart.setData(lineData);
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
        chart.setAutoScaleMinMaxEnabled(true);
    }

    private void setViewPort() {
        this.viewPortHandler.setMinMaxScaleY(1f,1f);
        this.viewPortHandler.setMinMaxScaleX(90f,1f);
        this.viewPortHandler.setDragOffsetX(15);
        ;
    }

    public void addEntry(Entry entry){
        LineData data = chart.getLineData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }
            set.setHighlightEnabled(true);
            data.addEntry(entry,0);

            data.notifyDataChanged();

            // let the chart know it's data has changed
            chart.notifyDataSetChanged();
            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(xRange);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);
            // move to the latest entry
            chart.moveViewToX(data.getXMax());
            lineDataSize++;
            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }


    private LineDataSet createSet() {
        String color=selectLinecolor();
        LineDataSet set = new LineDataSet(null, this.legendName);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setDrawCircles(false);
        set.setHighlightEnabled(true);
        set.setColor(ColorTemplate.rgb(color));
        set.setLineWidth(1f);
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


}

