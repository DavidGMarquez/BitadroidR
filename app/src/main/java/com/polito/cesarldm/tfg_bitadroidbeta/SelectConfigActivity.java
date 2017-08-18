package com.polito.cesarldm.tfg_bitadroidbeta;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.polito.cesarldm.tfg_bitadroidbeta.adapters.ConfigListAdapter;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.ChannelConfiguration;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.JsonManager;

import java.util.ArrayList;
import java.util.List;

public class SelectConfigActivity extends AppCompatActivity implements View.OnClickListener,ListView.OnItemClickListener,ListView.OnItemLongClickListener {
    //UI
    Button btnNewConf;

    ListView configList;
    JsonManager jsonManager;
    ConfigListAdapter adapter;
    BluetoothDevice device;
    List<ChannelConfiguration> returnedList;
    ArrayList<ChannelConfiguration> channelConfList=new ArrayList<ChannelConfiguration>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent().hasExtra("Device")){
            device=getIntent().getParcelableExtra("Device");
        }else{
            device=null;
        }

        setContentView(R.layout.activity_select_config);
        configList=(ListView)findViewById(R.id.lv_SC);
        btnNewConf=(Button)findViewById(R.id.btn_SC_new_conf);
        jsonManager=new JsonManager();
        btnNewConf.setOnClickListener(this);
        adapter=new ConfigListAdapter(this);
        configList.setAdapter(adapter);
        configList.setOnItemClickListener(this);
        configList.setOnItemLongClickListener(this);
        listConfigurations();

    }


    @Override
    protected void onStart(){
        super.onStart();
       listConfigurations();



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
    public void onClick(View v) {
        Intent bthIntent = new Intent(this, CreateConfigActivity.class);
        startActivityForResult(bthIntent, 1);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ChannelConfiguration selectedChannelConfig=adapter.getItem(position);
        if(selectedChannelConfig.getRecordingChannels().length==1){
            Intent intentStartSingle = new Intent(this, ShowSingleDataActivity.class);
            intentStartSingle.putExtra("Device",device);
            intentStartSingle.putExtra("Config",selectedChannelConfig);
            startActivity(intentStartSingle);
        }else {
            Intent intentStart = new Intent(this, ShowDataActivity.class);
            intentStart.putExtra("Device", device);
            intentStart.putExtra("Config", selectedChannelConfig);
            startActivity(intentStart);
        }
    }
    public void listConfigurations(){
        returnedList=jsonManager.getCurrentChannelConfigs();
        if(returnedList!=null) {
            channelConfList.clear();
            channelConfList.addAll(returnedList);
            adapter.setConfigArray(channelConfList);
            adapter.notifyDataSetChanged();
        }

    }

    /**
     * RECEIVES THE CONFIGURATION CREATED BY THE USER
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == SelectDevicesActivity.RESULT_OK){
                Bundle b=data.getExtras();
                ChannelConfiguration mChannelConfig=b.getParcelable("result");
                toastMessageLong("N: "+mChannelConfig.getName() +"S.R= "+mChannelConfig.sampleRate+" Sz: "+mChannelConfig.getSize());
                adapter.addConfiguration(mChannelConfig);
                adapter.notifyDataSetChanged();

            }
            if (resultCode == SelectDevicesActivity.RESULT_CANCELED) {

                toastMessageShort("No configuration created");
            }
        }
    }

    /**
     * TOAST METHODS
     */
    public void toastMessageShort(String a) {
        Toast.makeText(this, a, Toast.LENGTH_SHORT).show();

    }

    public void toastMessageLong(String a) {
        Toast.makeText(this, a, Toast.LENGTH_LONG).show();

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        ChannelConfiguration selectedChannelConfig=adapter.getItem(position);
        jsonManager.deleteConfiguration(position);
        listConfigurations();

        return true;
    }
}
