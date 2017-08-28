package com.polito.cesarldm.tfg_bitadroidbeta;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.github.mikephil.charting.data.Entry;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.MPAndroidGraph;

import java.util.ArrayList;

public class PopBPMRRGraphActivity extends AppCompatActivity  {
    MPAndroidGraph mpAndroidGraphBPM,mpAndroidGraphRR;

    ArrayList<Entry> rrValues=new ArrayList<Entry>();
    ArrayList<Entry> bpmValues=new ArrayList<Entry>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_bpmrrgraph);
        DisplayMetrics displayMetrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width=displayMetrics.widthPixels;
        int height=displayMetrics.heightPixels;
        //getWindow().setLayout((int)(width*0.8),(int)(height*0.6));
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setActivityLayout();
        bpmValues=getIntent().getParcelableArrayListExtra("bpm");
        rrValues=getIntent().getParcelableArrayListExtra("rr");
        setChartValues();

    }


    private void setActivityLayout() {

        ViewGroup.LayoutParams layoutParams=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mpAndroidGraphBPM=new MPAndroidGraph(this,"BPM",0);
        RelativeLayout rl1=(RelativeLayout)findViewById(R.id.rl_PU_graphBPM);
        mpAndroidGraphBPM.getGraphView().setLayoutParams(layoutParams);
        rl1.addView(mpAndroidGraphBPM.getGraphView());

        mpAndroidGraphRR=new MPAndroidGraph(this,"RR",1);
        RelativeLayout rl=(RelativeLayout)findViewById(R.id.rl_PU_graphRR);
        mpAndroidGraphRR.getGraphView().setLayoutParams(layoutParams);
        rl.addView(mpAndroidGraphRR.getGraphView());
    }
    private void setChartValues() {
        for(int i=0;i<bpmValues.size();i++){
            mpAndroidGraphBPM.addEntry(bpmValues.get(i));
        }
        for(int i=0;i<rrValues.size();i++){
            mpAndroidGraphRR.addEntry(rrValues.get(i));
        }

    }


}
