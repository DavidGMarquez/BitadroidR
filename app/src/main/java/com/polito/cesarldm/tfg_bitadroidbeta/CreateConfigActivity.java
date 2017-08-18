package com.polito.cesarldm.tfg_bitadroidbeta;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.polito.cesarldm.tfg_bitadroidbeta.vo.ChannelConfiguration;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.JsonManager;

import java.util.ArrayList;

public class CreateConfigActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {
    //UI
    Switch chn1,chn2,chn3,chn4,chn5,chn6,chs1,chs2,chs3,chs4,chs5,chs6;
    RadioGroup radioGroup;
    RadioButton freq1,freq10,freq100,freq1000;
    Button btnDone;
    EditText etConfigName;
    Spinner sp1,sp2,sp3,sp4,sp5,sp6;
    int sampleRate=10;
    int visualizationRate;
    ChannelConfiguration mChannelConf;
    ArrayList<Integer> channelsToShow=new ArrayList<Integer>();
    ArrayList<Integer> channelsSelected=new ArrayList<Integer>();
    ArrayList<String> activeChannelsNames=new ArrayList<String>();
    ArrayList<String> channelsToShowNames=new ArrayList<String>();
    int[] analogChannels=new int[6];
    int[] shownChannels=new int [6];
    String[] analogNames=new String[6];
    String[] shownNames=new String[6];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_config);
        setSwitches();
        setRadioButtons();
        setSpinners();
        btnDone=(Button) findViewById(R.id.btn_CC_done);
        btnDone.setOnClickListener(this);
        etConfigName=(EditText)findViewById(R.id.et_CC_name);

    }

    private void setSpinners() {
        sp1=(Spinner)findViewById(R.id.spinner1);
        sp2=(Spinner)findViewById(R.id.spinner2);
        sp3=(Spinner)findViewById(R.id.spinner3);
        sp4=(Spinner)findViewById(R.id.spinner4);
        sp5=(Spinner)findViewById(R.id.spinner5);
        sp6=(Spinner)findViewById(R.id.spinner6);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.channels_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp1.setAdapter(adapter);
        sp2.setAdapter(adapter);
        sp3.setAdapter(adapter);
        sp4.setAdapter(adapter);
        sp5.setAdapter(adapter);
        sp6.setAdapter(adapter);
        sp1.setSelection(0);
        sp2.setSelection(1);
        sp3.setSelection(2);
        sp4.setSelection(3);
        sp5.setSelection(4);
        sp6.setSelection(5);

    }

    private void setRadioButtons() {
        radioGroup=(RadioGroup)findViewById(R.id.rg_CC);
        freq1=(RadioButton)findViewById(R.id.radioButton1);
        freq10=(RadioButton)findViewById(R.id.radioButton10);
        freq100=(RadioButton)findViewById(R.id.radioButton100);
        freq1000=(RadioButton)findViewById(R.id.radioButton1000);
        radioGroup.setOnCheckedChangeListener(this);
    }

    private void setSwitches() {
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
        chs1=(Switch)findViewById(R.id.switch21);
        chs1.setChecked(false);
        chs2=(Switch)findViewById(R.id.switch22);
        chs2.setChecked(false);
        chs3=(Switch)findViewById(R.id.switch23);
        chs3.setChecked(false);
        chs4=(Switch)findViewById(R.id.switch24);
        chs4.setChecked(false);
        chs5=(Switch)findViewById(R.id.switch25);
        chs5.setChecked(false);
        chs6=(Switch)findViewById(R.id.switch26);
        chs6.setChecked(false);
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
        addSelectedChannels();
        addShownChannels();
        analogChannels=convertIntegers(channelsSelected);
        shownChannels=convertIntegers(channelsToShow);
        analogNames=activeChannelsNames.toArray(new String[0]);
        shownNames=channelsToShowNames.toArray(new String[0]);
        String tempName=etConfigName.getText().toString();
        mChannelConf=new ChannelConfiguration(tempName,analogChannels,shownChannels,sampleRate,analogNames,shownNames);
        JsonManager jsonManager=new JsonManager(this,mChannelConf);
        Intent returnIntentOne = new Intent();
        returnIntentOne.putExtra("result", mChannelConf);
        setResult(SelectDevicesActivity.RESULT_OK, returnIntentOne);
        finish();

    }

    private void addSelectedChannels() {
        if(chn1.isChecked()){
            channelsSelected.add(0);
            activeChannelsNames.add(sp1.getSelectedItem().toString());
        }
        if(chn2.isChecked()){
            channelsSelected.add(1);
            activeChannelsNames.add(sp2.getSelectedItem().toString());
        }
        if(chn3.isChecked()){
            channelsSelected.add(2);
            activeChannelsNames.add(sp3.getSelectedItem().toString());
        }
        if(chn4.isChecked()){
            channelsSelected.add(3);
            activeChannelsNames.add(sp4.getSelectedItem().toString());
        }
        if(chn5.isChecked()){
            channelsSelected.add(4);
            activeChannelsNames.add(sp5.getSelectedItem().toString());
        }
        if(chn6.isChecked()){
            channelsSelected.add(5);
            activeChannelsNames.add(sp6.getSelectedItem().toString());
        }
    }
    private void addShownChannels(){
        if(chs1.isChecked()){
            channelsToShow.add(0);
            channelsToShowNames.add(sp1.getSelectedItem().toString());
        }
        if(chs2.isChecked()){
            channelsToShow.add(1);
            channelsToShowNames.add(sp2.getSelectedItem().toString());
        }
        if(chs3.isChecked()){
            channelsToShow.add(2);
            channelsToShowNames.add(sp3.getSelectedItem().toString());
        }
        if(chs4.isChecked()){
            channelsToShow.add(3);
            channelsToShowNames.add(sp4.getSelectedItem().toString());
        }
        if(chs5.isChecked()){
            channelsToShow.add(4);
            channelsToShowNames.add(sp5.getSelectedItem().toString());
        }
        if(chs6.isChecked()){
            channelsToShow.add(5);
            channelsToShowNames.add(sp6.getSelectedItem().toString());
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
