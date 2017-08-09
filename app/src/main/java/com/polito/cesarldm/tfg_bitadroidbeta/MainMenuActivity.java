package com.polito.cesarldm.tfg_bitadroidbeta;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import info.plux.pluxapi.bitalino.BITalinoDescription;
import info.plux.pluxapi.bitalino.BITalinoState;

public class MainMenuActivity extends AppCompatActivity implements View.OnClickListener{

    //UI Elements
    Button btnScanDev,btnStartRec,btnShowRec;
    TextView tvDeviceName,tvDeviceMac;
    //Deice selected by user
    private BluetoothDevice device;
    private BITalinoState deviceState;
    private BITalinoDescription deviceDesc;
    final int BLUETOOTH_INTENT=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        tvDeviceName=(TextView)findViewById(R.id.tv_MM_name);
        tvDeviceMac=(TextView)findViewById(R.id.tv_MM_mac);
        btnStartRec=(Button) findViewById(R.id.btn_MM_start_recordings);
        btnShowRec=(Button) findViewById(R.id.btn_MM_show_recordings);
        btnScanDev=(Button) findViewById(R.id.btn_MM_scan);
        btnScanDev.setOnClickListener(this);
        btnStartRec.setOnClickListener(this);
        btnShowRec.setOnClickListener(this);
    }
    @Override
    protected void onStart(){
        super.onStart();
        tvDeviceName.setText("Device not selected");
        tvDeviceMac.setText("MAC: 00:00:00:00:00:00");

    }

    @Override
    protected void onResume(){
        super.onResume();
        if(device!=null){
            tvDeviceName.setText("NAME: "+device.getName());
            tvDeviceMac.setText("MAC: "+device.getAddress());

        }

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
        switch(v.getId()){
            case R.id.btn_MM_scan:
                Intent bthIntent=new Intent(this,SelectDevicesActivity.class);
                startActivityForResult(bthIntent,BLUETOOTH_INTENT);
                break;
            case R.id.btn_MM_start_recordings:
                Intent configIntent=new Intent(this,SelectConfigActivity.class);
                configIntent.putExtra("Device",device);
                startActivity(configIntent);
                break;
            case R.id.btn_MM_show_recordings:
                Intent recordIntent=new Intent(this,SelectRecordingActivity.class);
                recordIntent.putExtra("Device",device);
                startActivity(recordIntent);
                break;


        }

    }

    /**
     * RECEIVES THE BLUETOOTH DEVICE SELECTED BY THE USER
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == SelectDevicesActivity.RESULT_OK){
                Bundle b=data.getExtras();
                device=b.getParcelable("result");
                deviceDesc=b.getParcelable("Desc");
                //connect
                toastMessageShort(device.getName() + ", MAC= " + device.getAddress());
            }
            if (resultCode == SelectDevicesActivity.RESULT_CANCELED) {
                device=null;
                deviceDesc=null;
                toastMessageShort("No Bitalino device selected");
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
}
