package com.polito.cesarldm.tfg_bitadroidbeta;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.Object;
import android.util.JsonWriter;
import com.polito.cesarldm.tfg_bitadroidbeta.R;
import com.polito.cesarldm.tfg_bitadroidbeta.beats.SampleRate;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.ChannelConfiguration;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.JsonManager;

import java.util.ArrayList;

public class CreateConfigActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {
    //UI
    Switch chn1,chn2,chn3,chn4,chn5,chn6,chs1,chs2,chs3,chs4,chs5,chs6;
    RadioGroup radioGroup,radioGroupV;
    RadioButton freq1,freq10,freq100,freq1000,freq1v,freq10v,freq100v;
    Button btnDone;
    EditText etConfigName;
    Spinner sp1,sp2,sp3,sp4,sp5,sp6;
    int sampleRate=10;
    int visualizationRate=10;
    private AlertDialog alertDialogCheckBitalino;
    ChannelConfiguration mChannelConf;
    Switch[] actChn=new Switch[6];
    Switch[] shnChn=new Switch[6];
    Spinner[] spnList=new Spinner[6];
    ArrayList<Integer> channelsToShow=new ArrayList<Integer>();
    ArrayList<Integer> channelsSelected=new ArrayList<Integer>();
    ArrayList<String> activeChannelsNames=new ArrayList<String>();
    ArrayList<String> channelsToShowNames=new ArrayList<String>();
    int[] analogChannels=new int[6];
    int[] shownChannels=new int [6];
    String[] analogNames=new String[6];
    String[] shownNames=new String[6];
    boolean isUpdate=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_config);
        if(getIntent().hasExtra("Config")) {
            mChannelConf = getIntent().getParcelableExtra("Config");
        }
        setSwitches();
        setRadioButtons();
        setSpinners();
        btnDone=(Button) findViewById(R.id.btn_CC_done);
        btnDone.setOnClickListener(this);
        etConfigName=(EditText)findViewById(R.id.et_CC_name);
        alertDialogInitiate();
        if(getIntent().hasExtra("Config")) {
            mChannelConf = getIntent().getParcelableExtra("Config");
            editVariables(mChannelConf);
            isUpdate=true;
        }
    }

    private void editVariables(ChannelConfiguration mChannelConf) {
       String[] names=getResources().getStringArray(R.array.channels_array);
        etConfigName.setText(mChannelConf.getName());
        for(int i=0;i<6;i++){
            for(int j=0;j<mChannelConf.getActiveChannelListSize();j++){
                if(i==mChannelConf.activeChannels[j]){
                    actChn[i].setChecked(true);
                    for(int y=0; y<names.length;y++){
                        if(mChannelConf.activeChannelsNames[j].equals(names[y])){
                            spnList[i].setSelection(y);
                        }
                    }
                }
            }
            for(int z=0;z<mChannelConf.getRecordingChannels().length;z++){
                if(i==mChannelConf.recordingChannels[z]){
                    shnChn[i].setChecked(true);
                }
            }
        }
        switch (mChannelConf.getSampleRate()){
            case 1:
               freq1.toggle();
                break;
            case 10:
              freq10.toggle();
                break;
            case 100:
                freq100.toggle();
                break;
            case 1000:
              freq1000.toggle();
                break;
            default:
               freq10.toggle();
        }
        switch (mChannelConf.getVisualizationRate()){
            case 1:
                freq1v.toggle();
                break;
            case 10:
                freq10v.toggle();
                break;
            case 100:
                freq100v.toggle();
                break;
            default:
                freq10v.toggle();
        }
    }

    private void alertDialogInitiate() {
        alertDialogCheckBitalino=new AlertDialog.Builder(CreateConfigActivity.this).create();
        alertDialogCheckBitalino.setTitle(Html.fromHtml("<font color='#F44E42'>Warning</font>"));
        alertDialogCheckBitalino.setMessage(Html.fromHtml("<font color='#F44E42'>Some devices may suffer from performance issues using " +
                "the selected configuration. Do you want to set the visualization rate to 10Hz?</font>"));
        alertDialogCheckBitalino.setButton(Dialog.BUTTON_POSITIVE,Html.fromHtml("<font color='#F44E42'>YES</font>"),new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int which){
                mChannelConf.setVisulizationRate(10);
                endActivity();


            }
        });

        alertDialogCheckBitalino.setButton(Dialog.BUTTON_NEGATIVE,Html.fromHtml("<font color='#F44E42'>NO</font>"),new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int which){
                endActivity();
            }
        });
        alertDialogCheckBitalino.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                alertDialogCheckBitalino.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAlert));
                alertDialogCheckBitalino.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorAlert));
            }
        });

        alertDialogCheckBitalino.setIcon(R.drawable.ic_fail);

    }

    private void setSpinners() {
        sp1=(Spinner)findViewById(R.id.spinner1);
        spnList[0]=sp1;
        sp2=(Spinner)findViewById(R.id.spinner2);
        spnList[1]=sp2;
        sp3=(Spinner)findViewById(R.id.spinner3);
        spnList[2]=sp3;
        sp4=(Spinner)findViewById(R.id.spinner4);
        spnList[3]=sp4;
        sp5=(Spinner)findViewById(R.id.spinner5);
        spnList[4]=sp5;
        sp6=(Spinner)findViewById(R.id.spinner6);
        spnList[5]=sp6;
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.channels_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for(int i=0;i<6;i++){
            spnList[i].setAdapter(adapter);
            spnList[i].setSelection(i);
        }
    }
    private void setRadioButtons() {
        radioGroup=(RadioGroup)findViewById(R.id.rg_CC);
        freq1=(RadioButton)findViewById(R.id.radioButton1);
        freq10=(RadioButton)findViewById(R.id.radioButton10);
        freq100=(RadioButton)findViewById(R.id.radioButton100);
        freq1000=(RadioButton)findViewById(R.id.radioButton1000);
        radioGroup.setOnCheckedChangeListener(this);
        radioGroupV=(RadioGroup)findViewById(R.id.rgShown_CC);
        freq1v=(RadioButton)findViewById(R.id.radioSButton1);
        freq10v=(RadioButton)findViewById(R.id.radioSButton10);
        freq100v=(RadioButton)findViewById(R.id.radioSButton100);
        radioGroupV.setOnCheckedChangeListener(this);
    }

    private void setSwitches() {
        chn1=(Switch)findViewById(R.id.switch1);
        chn1.setChecked(false);
        actChn[0]=chn1;
        chn2=(Switch)findViewById(R.id.switch2);
        chn2.setChecked(false);
        actChn[1]=chn2;
        chn3=(Switch)findViewById(R.id.switch3);
        chn3.setChecked(false);
        actChn[2]=chn3;
        chn4=(Switch)findViewById(R.id.switch4);
        chn4.setChecked(false);
        actChn[3]=chn4;
        chn5=(Switch)findViewById(R.id.switch5);
        chn5.setChecked(false);
        actChn[4]=chn5;
        chn6=(Switch)findViewById(R.id.switch6);
        chn6.setChecked(false);
        actChn[5]=chn6;
        chs1=(Switch)findViewById(R.id.switch21);
        chs1.setChecked(false);
        shnChn[0]=chs1;
        chs2=(Switch)findViewById(R.id.switch22);
        chs2.setChecked(false);
        shnChn[1]=chs2;
        chs3=(Switch)findViewById(R.id.switch23);
        chs3.setChecked(false);
        shnChn[2]=chs3;
        chs4=(Switch)findViewById(R.id.switch24);
        chs4.setChecked(false);
        shnChn[3]=chs4;
        chs5=(Switch)findViewById(R.id.switch25);
        chs5.setChecked(false);
        shnChn[4]=chs5;
        chs6=(Switch)findViewById(R.id.switch26);
        chs6.setChecked(false);
        shnChn[5]=chs6;
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
                if(visualizationRate>1){
                    visualizationRate=1;
                    freq1v.setChecked(true);
                }
                sampleRate=1;
                break;
            case R.id.radioButton10:
                if(visualizationRate>10){
                    visualizationRate=10;
                    freq10v.setChecked(true);
                }
                sampleRate=10;
                break;
            case R.id.radioButton100:
                if(visualizationRate>100){
                    visualizationRate=100;
                    freq100v.setChecked(true);
                }
                sampleRate=100;
                break;
            case R.id.radioButton1000:
                sampleRate=1000;
                break;
            case R.id.radioSButton1:
                visualizationRate=1;
                break;
            case R.id.radioSButton10:
                if(sampleRate>=10) {
                    visualizationRate = 10;
                }else{
                    visualizationRate=sampleRate;
                    freq1v.setChecked(true);
                }
                break;
            case R.id.radioSButton100:
                if(sampleRate>=100) {
                    visualizationRate = 100;
                }else{
                    visualizationRate=sampleRate;
                    if(sampleRate==10){
                        freq10v.setChecked(true);
                    }else{
                        freq1v.setChecked(true);
                    }
                }
                break;
            default:
                sampleRate=10;
                visualizationRate=10;
        }


    }

    @Override
    public void onClick(View v) {
        channelsSelected.clear();
        activeChannelsNames.clear();
        addSelectedChannels();
        addShownChannels();
        analogChannels=convertIntegers(channelsSelected);
        shownChannels=convertIntegers(channelsToShow);
        analogNames=activeChannelsNames.toArray(new String[0]);
        shownNames=channelsToShowNames.toArray(new String[0]);
        String tempName=etConfigName.getText().toString();
        mChannelConf=new ChannelConfiguration(tempName,analogChannels,shownChannels,sampleRate,visualizationRate,analogNames,shownNames);
        noticeUser(mChannelConf);

    }
    private void noticeUser(ChannelConfiguration mConfig) {
        if(mConfig.getVisualizationRate()==100 && mConfig.getRecordingChannels().length>=3){
            alertDialogCheckBitalino.show();

        }else{
            endActivity();
        }
    }
    private void endActivity() {
        if(!isUpdate){
            JsonManager jsonManager=new JsonManager(this,mChannelConf);
        }
        Intent returnIntentOne = new Intent();
        returnIntentOne.putExtra("result", mChannelConf);
        setResult(SelectDevicesActivity.RESULT_OK, returnIntentOne);
        finish();
    }

    private void addSelectedChannels() {
        for(int i=0;i<6;i++) {
            if (actChn[i].isChecked()) {
                channelsSelected.add(i);
                activeChannelsNames.add(spnList[i].getSelectedItem().toString());
            }
        }

    }
    private void addShownChannels(){
        for(int i=0;i<6;i++) {
            if (shnChn[i].isChecked()) {
                channelsToShow.add(i);
                channelsToShowNames.add(spnList[i].getSelectedItem().toString());
            }
        }

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
