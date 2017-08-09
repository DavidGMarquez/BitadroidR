package com.polito.cesarldm.tfg_bitadroidbeta;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.Object;
import android.util.JsonWriter;
import com.polito.cesarldm.tfg_bitadroidbeta.R;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.ChannelConfiguration;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.JsonManager;

import java.util.ArrayList;

public class CreateConfigActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {
    //UI
    Switch chn1,chn2,chn3,chn4,chn5,chn6;
    RadioGroup radioGroup;
    RadioButton freq1,freq10,freq100,freq1000;
    Button btnDone;
    EditText etConfigName;
    int sampleRate=10;
    int visualizationRate;
    ChannelConfiguration mChannelConf;
    ArrayList<Integer> channelsSelected=new ArrayList<Integer>();
    ArrayList<String> activeChannelsNames=new ArrayList<String>();
    int[] analogChannels=new int[6];
    String[] analogNames=new String[6];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_config);
        chn1=(Switch)findViewById(R.id.switch1);
        chn1.setChecked(false);
        chn2=(Switch)findViewById(R.id.switch2);
        chn2.setChecked(false);
        chn3=(Switch)findViewById(R.id.switch3);
        chn3.setChecked(false);
        chn4=(Switch)findViewById(R.id.switch4);
        chn4.setChecked(false);
        chn5=(Switch)findViewById(R.id.switch5);
        chn5.setChecked(false);
        chn6=(Switch)findViewById(R.id.switch6);
        chn6.setChecked(false);
        radioGroup=(RadioGroup)findViewById(R.id.rg_CC);
        freq1=(RadioButton)findViewById(R.id.radioButton1);
        freq10=(RadioButton)findViewById(R.id.radioButton10);
        freq100=(RadioButton)findViewById(R.id.radioButton100);
        freq1000=(RadioButton)findViewById(R.id.radioButton100);
        radioGroup.setOnCheckedChangeListener(this);
        btnDone=(Button) findViewById(R.id.btn_CC_done);
        btnDone.setOnClickListener(this);
        etConfigName=(EditText)findViewById(R.id.et_CC_name);

    }
    @Override
    protected void onStart(){
        super.onStart();

    }
    @Override
    protected void onResume(){
        super.onResume();
    }
    @Override
    protected void onPause(){
        super.onPause();
    }
    @Override
    protected void onStop(){
        super.onStop();

    }
    @Override
    protected void onDestroy(){
        super.onDestroy();

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){
            case R.id.radioButton1:
                sampleRate=1;
                break;
            case R.id.radioButton10:
                sampleRate=10;
                break;
            case R.id.radioButton100:
                sampleRate=100;
                break;
            case R.id.radioButton1000:
                sampleRate=1000;
                break;
            default:
                sampleRate=10;
        }


    }

    @Override
    public void onClick(View v) {
        channelsSelected.clear();
        activeChannelsNames.clear();
        if(chn1.isChecked()){
            channelsSelected.add(0);
            activeChannelsNames.add(chn1.getText().toString());
        }
        if(chn2.isChecked()){
            channelsSelected.add(1);
            activeChannelsNames.add(chn2.getText().toString());
        }
        if(chn3.isChecked()){
            channelsSelected.add(2);
            activeChannelsNames.add(chn3.getText().toString());
        }
        if(chn4.isChecked()){
            channelsSelected.add(3);
            activeChannelsNames.add(chn4.getText().toString());
        }
        if(chn5.isChecked()){
            channelsSelected.add(4);
            activeChannelsNames.add(chn5.getText().toString());
        }
        if(chn6.isChecked()){
            channelsSelected.add(5);
            activeChannelsNames.add(chn6.getText().toString());
        }
        analogChannels=convertIntegers(channelsSelected);
        analogNames=activeChannelsNames.toArray(new String[0]);
        String tempName=etConfigName.getText().toString();
        mChannelConf=new ChannelConfiguration(tempName,analogChannels,sampleRate,analogNames);
        JsonManager jsonManager=new JsonManager(this,mChannelConf);
        Intent returnIntentOne = new Intent();
        returnIntentOne.putExtra("result", mChannelConf);
        setResult(SelectDevicesActivity.RESULT_OK, returnIntentOne);


        finish();

    }

    public static int[] convertIntegers(ArrayList<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = integers.get(i).intValue();
        }
        return ret;
    }

}
