package com.polito.cesarldm.tfg_bitadroidbeta.vo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.jobs.MoveViewJob;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.polito.cesarldm.tfg_bitadroidbeta.R;
import com.polito.cesarldm.tfg_bitadroidbeta.ShowDataActivity;

import java.util.List;

/**
 * Created by Cesar on 09/11/2017.
 */

public class Linechart implements OnChartGestureListener, View.OnClickListener,OnChartValueSelectedListener {
    private List<Entry> entries;
    private Context context;
    private ILineDataSet dataSet;
    private LineChart chart;
    private XAxis xAxis;
    private YAxis yAxis;
    //private LineData lineData;
    private String legendName;
    private int channelNum;
    private ChannelConfiguration mConfiguration;
    private int xRange;
    float f;
    private int zoomValue=5;
    boolean zoomFinished=false;
    float xscale;
    public Entry selectedentry;
    private float chartScale;
    private int zoomCount=0;
    LineData data;
    ILineDataSet set;



    public Linechart(Context context, ChannelConfiguration mConfiguration, int position){
        this.context=context;
        this.selectedentry=new Entry(0,0);
        this.mConfiguration=mConfiguration;
        this.chart=new LineChart(context);
        this.legendName=mConfiguration.shownNames[position];
        this.channelNum=mConfiguration.recordingChannels[position];
        setChartCharacteristics();
        this.chart.setOnChartValueSelectedListener(this);

    }

    public Linechart(Context context,String name,int position){
        this.context=context;
        this.selectedentry=new Entry(0,0);
        this.chart=new LineChart(context);
        this.legendName=name;
        setChartCharacteristics();


    }
    private void setChartCharacteristics() {
        this.chart.setData(new LineData());
        this.xAxis=this.chart.getXAxis();
        this.yAxis=this.chart.getAxisLeft();
        Legend l=chart.getLegend();
        l.setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleXEnabled(true);
        chart.setScaleYEnabled(false);
        chart.setPinchZoom(true);
        chart.setDoubleTapToZoomEnabled(true);
        chart.setOnChartGestureListener(this);
        chart.setHardwareAccelerationEnabled(true);
        setAxisCharacteristics();
        Description desc=new Description();
        desc.setText(this.legendName);
        chart.setDescription(desc);
        chart.setAutoScaleMinMaxEnabled(true);
        this.data = this.chart.getData();
        chart.invalidate();
    }
    private void setAxisCharacteristics() {
        //X axis
        xAxis.setEnabled(true);
        xAxis.setDrawLabels(true);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true);
        xAxis.setValueFormatter(new MyAxisValueFormatter());
        xAxis.setLabelCount(3,true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //Y axis
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxis.setLabelCount(6,true);


    }


    public void addEntry(float xvalue,float value){

        if (data != null) {
            set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(xvalue,value),0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            this.chart.notifyDataSetChanged();
            this.zoomCount++;

            // limit the number of visible entries

           if(this.zoomCount==mConfiguration.getVisualizationRate()*5 && !zoomFinished){
               chart.fitScreen();
               chart.zoomToCenter(1/((float)mConfiguration.getVisualizationRate()*this.zoomValue/chart.getLineData().getEntryCount()),1f);
               this.zoomCount=0;
           }

           // this.chart.setVisibleXRangeMaximum(zoomValue);

            // this.chart.setVisibleYRange(30, AxisDependency.LEFT);
            // move to the latest entry
            this.chart.moveViewToX(data.getXMax());
            //this.chart.moveViewToX(data.getEntryCount());
            // this automatically refreshes the chart (calls invalidate())
            // this.chart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }
    public void addStaticEntry(Entry entry){

        LineData data = this.chart.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(entry,0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            this.chart.notifyDataSetChanged();


            // limit the number of visible entries


            // this.chart.setVisibleXRangeMaximum(zoomValue);

            // this.chart.setVisibleYRange(30, AxisDependency.LEFT);
            // move to the latest entry
            this.chart.moveViewToX(data.getXMax());
            //this.chart.moveViewToX(data.getEntryCount());
            // this automatically refreshes the chart (calls invalidate())
            // this.chart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }
    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null,this.legendName);

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.rgb(selectLinecolor()));
        set.setDrawCircles(false);
        set.setLineWidth(2f);
        //set.setFillAlpha(65);
        set.setHighLightColor(Color.rgb(244, 117, 117));
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
        zoomFinished=false;
        zoomValue=5;
        chart.fitScreen();
        chart.zoomToCenter(1/((float)mConfiguration.getVisualizationRate()*this.zoomValue/chart.getLineData().getEntryCount()),1f);
        chart.invalidate();
    }
   public void zoomIn(){
       if(this.zoomValue>1){
           zoomFinished=false;
           zoomValue--;
           chart.fitScreen();
           chart.zoomToCenter(1/((float)mConfiguration.getVisualizationRate()*this.zoomValue/chart.getLineData().getEntryCount()),1f);
           chart.invalidate();
       }

   }
   public void zoomOut() {
      if(this.zoomValue<58) {
          zoomFinished=false;
          zoomValue++;
          zoomValue++;
          chart.fitScreen();
          chart.zoomToCenter(1/((float)mConfiguration.getVisualizationRate()*this.zoomValue/chart.getLineData().getEntryCount()),1f);
          chart.invalidate();
      }

   }



    public LineChart getGraphView(){return this.chart;}
    public float getYMax(){return this.chart.getYMax();}
    public float getYMin(){return this.chart.getYMin();}
    public float getDataSetSize(){
        return this.chart.getLineData().getDataSetCount();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        this.selectedentry=e;

    }

    @Override
    public void onNothingSelected() {

    }

    public Entry getSelectedValue(){return this.selectedentry;}

    public void setHighLightt(float xvalue){
    this.chart.highlightValue(xvalue,0);

    }


    @Override
    public void onClick(View v) {

    }



    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        chart.saveToGallery(mConfiguration.getName(),50);
        Toast.makeText(this.context,"Snapshot saved to gallery",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {

    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        zoomFinished=true;
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    public String getEntryCount() {
        return String.valueOf(this.chart.getLineData().getEntryCount());

    }

    public void cleanPool(){
        //avoid memory leak:
         MoveViewJob.getInstance(null, 0f, 0f, null, null);
         chart.clearAllViewportJobs();

    }
    public void deleteChart(){
      this.set.clear();
      this.data.clearValues();
      this.chart.clearValues();
      this.set=null;
      this.data=null;
      this.chart=null;
    }

    public float getdataSize() {
        return this.chart.getLineData().getEntryCount();
    }
}

