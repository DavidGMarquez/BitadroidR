package com.polito.cesarldm.tfg_bitadroidbeta;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.polito.cesarldm.tfg_bitadroidbeta.vo.ChannelConfiguration;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.JsonManager;

import java.util.ArrayList;

public class CtreateHMConfigActivity extends Activity implements View.OnClickListener{
    Switch chn1,chn2,chn3,chn4,chn5,chn6;
    Button btnDone;
    Spinner sp1,sp2,sp3,sp4,sp5,sp6;
    ChannelConfiguration mChannelConf;
    ArrayList<Integer> channelsToShow=new ArrayList<Integer>();
    ArrayList<Integer> channelsSelected=new ArrayList<Integer>();
    ArrayList<String> activeChannelsNames=new ArrayList<String>();
    ArrayList<String> channelsToShowNames=new ArrayList<String>();
    ArrayList<Spinner> spinerValue=new ArrayList<Spinner>();
    int[] analogChannels=new int[6];
    int[] shownChannels=new int [6];
    String[] analogNames=new String[6];
    String[] shownNames=new String[6];
    BluetoothDevice device;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ctreate_hmconfig);
        DisplayMetrics displayMetrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width=displayMetrics.widthPixels;
        int height=displayMetrics.heightPixels;
        //getWindow().setLayout((int)(width*0.8),(int)(height*0.6));
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (getIntent().hasExtra("Device")) {
            device = getIntent().getParcelableExtra("Device");
        } else {
            device = null;
        }
        setSwitches();
        setSpinners();
        btnDone=(Button) findViewById(R.id.btn_CC_done);
        btnDone.setOnClickListener(this);
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
        spinerValue.add(sp1);
        sp2.setSelection(1);
        spinerValue.add(sp2);
        sp3.setSelection(2);
        spinerValue.add(sp3);
        sp4.setSelection(3);
        spinerValue.add(sp4);
        sp5.setSelection(4);
        spinerValue.add(sp5);
        sp6.setSelection(5);
        spinerValue.add(sp6);

    }
    private void setSwitches() {
        chn1 = (Switch) findViewById(R.id.switch1);
        chn1.setChecked(false);
        chn2 = (Switch) findViewById(R.id.switch2);
        chn2.setChecked(true);
        chn3 = (Switch) findViewById(R.id.switch3);
        chn3.setChecked(false);
        chn4 = (Switch) findViewById(R.id.switch4);
        chn4.setChecked(false);
        chn5 = (Switch) findViewById(R.id.switch5);
        chn5.setChecked(false);
        chn6 = (Switch) findViewById(R.id.switch6);
        chn6.setChecked(false);
    }

    @Override
    public void onClick(View v) {
        channelsSelected.clear();
        activeChannelsNames.clear();
        addSelectedChannels();
        if(addShownChannels()) {
            analogChannels = convertIntegers(channelsSelected);
            shownChannels = convertIntegers(channelsToShow);
            analogNames = activeChannelsNames.toArray(new String[0]);
            shownNames = channelsToShowNames.toArray(new String[0]);
            String tempName = "HM Recording";
            mChannelConf = new ChannelConfiguration(tempName, analogChannels, shownChannels, 100, analogNames, shownNames);
            endActivity();
        }


    }

    private void endActivity() {
        Intent hMIntent =new Intent(this,HeartMonitorActivity.class);
        ChannelConfiguration heartMonitor=new ChannelConfiguration("Heart Monitor",new int[]
                {1},new int[] {1},100,new String[] {"ECG"},new String[] {"ECG"});
        hMIntent.putExtra("Device",device);
        hMIntent.putExtra("Config",mChannelConf);
        startActivity(hMIntent);
        this.finish();
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
    private boolean addShownChannels(){
       int j=0;
        for(int i=0;i<spinerValue.size();i++){
            if(spinerValue.get(i).getSelectedItem().toString().equals("ECG")){
                channelsToShow.add(i);
                channelsToShowNames.add(spinerValue.get(i).getSelectedItem().toString());
                j++;
            }
        }
        if(j!=1||channelsSelected.size()==0){
            Toast.makeText(this,"Configuration not valid",Toast.LENGTH_SHORT).show();
            Toast.makeText(this,"Select ONE channel as ECG",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
