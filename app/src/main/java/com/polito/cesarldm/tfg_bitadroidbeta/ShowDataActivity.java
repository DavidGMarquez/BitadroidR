package com.polito.cesarldm.tfg_bitadroidbeta;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;
import com.polito.cesarldm.tfg_bitadroidbeta.services.*;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.ChannelConfiguration;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.FrameTransferFunction;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.MPAndroidGraph;


import java.util.ArrayList;

import info.plux.pluxapi.bitalino.BITalinoFrame;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ShowDataActivity extends AppCompatActivity  implements View.OnClickListener {

    static final String TAG="SHOW DATA ACTIVITY";
    //UI
    Button btnStart, btnStop,btnMap;
    ArrayList<BITalinoFrame> frames=new ArrayList<BITalinoFrame>();
    ArrayList<MPAndroidGraph> graphs=new ArrayList<MPAndroidGraph>();
    ArrayList<Location> locations=new ArrayList<Location>();
    ScrollView scrollView;
    //ListView graphList;
    FrameTransferFunction frameTransFunc;
    private double samplingFrames;
    private double samplingCounter = 0;
    private double timeCounter = 0;
    float xValue=0;
    private int numberOfFrames;
    BluetoothDevice device;
    ChannelConfiguration mConfiguration;
    boolean mBound;
    boolean isVisible;
    boolean isConnected=false;
    private final Messenger activityMessenger = new Messenger(new IncomingHandler());
    Messenger mService = null;
    private LayoutInflater inflater;
    public ProgressDialog progressDialogConnecting;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_data);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(!getIntent().hasExtra("Device")||!getIntent().hasExtra("Config")){
           Toast.makeText(this, "No device or config,", Toast.LENGTH_SHORT).show();
            finish();
       }
        inflater = this.getLayoutInflater();

        //Solicitar permisos
        permissionCheck();
        if(getIntent().getParcelableExtra("Device")!=null) {
            device = getIntent().getParcelableExtra("Device");
            mConfiguration = getIntent().getParcelableExtra("Config");
            btnStart = (Button) findViewById(R.id.btn_SDA_start);
            btnStart.setOnClickListener(this);
            btnStop = (Button) findViewById(R.id.btn_SDA_stop);
            btnStop.setOnClickListener(this);
            btnMap=(Button) findViewById(R.id.bt_SDA_map);
            btnMap.setOnClickListener(this);
            scrollView=(ScrollView)findViewById(R.id.sc_SD);
            Intent intent = new Intent(this, BitalinoCommunicationService.class);
            //Intent intent = new Intent(this, BitalinoDataService.class);
            //intent.putExtra("Device", device);
            //intent.putExtra("Config", mConfiguration);

            //-----Part of code created by @author Carlos Marten, Bitadroid APP NewRecordingActivity
            samplingFrames = (double) mConfiguration.getSampleRate() / mConfiguration.getVisualizationRate();
            numberOfFrames = mConfiguration.getSampleRate();
            //*************************************************************************************
            setActivityLayout();
            startService(intent);
            progressDialogConnecting=new ProgressDialog(ShowDataActivity.this);
            progressDialogConnecting.setMessage("Connecting to Bitalino");
            frameTransFunc=new FrameTransferFunction(mConfiguration);
        }else {
            Toast.makeText(this, "No device selected ", Toast.LENGTH_SHORT).show();
            finish();
        }


    }
    private void setActivityLayout() {
        //ScrollView sc=(ScrollView)findViewById(R.id.sc_SD);
        LinearLayout ll=(LinearLayout)findViewById(R.id.ll_SD);
        //sc.addView(ll);
        LayoutParams graphParams,relativeParams;
       // View graphsView=findViewById(R.id.ll_SD);
        graphParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        relativeParams=new LayoutParams(LayoutParams.MATCH_PARENT,300);
        for(int i=0; i<mConfiguration.getSize();i++){
            graphs.add(new MPAndroidGraph(this,mConfiguration,i));
                    //mConfiguration.activeChannels[i],mConfiguration.activeChannelsNames[i]));
            RelativeLayout graph = (RelativeLayout) inflater.inflate(
                    R.layout.graph_layout, null);
            ll.addView(graph,graphParams);
            //graphs.get(i).getGraphView().setOnTouchListener(graphTouchListener);
            graph.addView(graphs.get(i).getGraphView(),relativeParams);
            //((ViewGroup)graphsView).addView(graph, graphParams);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, BitalinoCommunicationService.class), mConnection,
                Context.BIND_AUTO_CREATE);
        if(!isConnected){
            progressDialogConnecting.show();
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        isVisible=true;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onPause(){
        super.onPause();
        isVisible=false;
    }
    @Override
    protected  void onDestroy(){
        super.onDestroy();
        if(mBound) {
            this.unbindService(mConnection);
        }
        Intent intent = new Intent(this, BitalinoCommunicationService.class);
        stopService(intent);
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        this.finish();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_SDA_start:
                startRecording();
                Intent gpsIntent=new Intent(this,GPSService.class);
                startService(gpsIntent);
                break;
            case R.id.btn_SDA_stop:
                stopRecording();
                Intent gpsIntentStop=new Intent(this,GPSService.class);
                stopService(gpsIntentStop);
                break;
            case R.id.bt_SDA_map:
                Intent iMap=new Intent (this,PopMapActivity.class);
                if(locations!=null) {
                    iMap.putParcelableArrayListExtra("Locations", locations);
                }
                startActivity(iMap);
                break;
        }
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BitalinoCommunicationService.MSG_SEND_FRAME:
                    Bundle bf =msg.getData();
                    BITalinoFrame frame=bf.getParcelable("Frame");
                    appendData(frame);

                    break;
                case BitalinoCommunicationService.MSG_SEND_CONNECTION_OFF:
                    Toast.makeText(getApplicationContext(),"Device Disconnected",Toast.LENGTH_SHORT).show();

                    break;
                case BitalinoCommunicationService.MSG_ERROR:
                    switch(msg.arg1){
                        case BitalinoCommunicationService.CODE_ERROR_TXT:
                            Log.d(TAG,"TXT_ERROR");
                            break;
                        case BitalinoCommunicationService.CODE_ERROR_SAVING:
                            Log.d(TAG,"SAVING_ERROR");
                    }
                    break;
                case BitalinoCommunicationService.MSG_SEND_CONNECTION_ON:
                    progressDialogConnecting.dismiss();
                    break;
                case BitalinoCommunicationService.MSG_SEND_LOCATION:
                    Bundle bl=msg.getData();
                    Location location=bl.getParcelable("Location");
                    locations.add(location);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    private void appendData(BITalinoFrame frame) {
        if (samplingCounter++ >= samplingFrames) {
            //float[] conVal=frameTransFunc.getConvertedValues(frame);
            // calculates x value of graphs
            timeCounter++;
            xValue = (float)(timeCounter* 1000) / mConfiguration.getVisualizationRate();
            // gets default share preferences with multi-process flag
            if(isVisible) {
                for (int i = 0; i < graphs.size(); i++) {
                    if (isViewVisible(graphs.get(i).getGraphView())) {
                        float f = frame.getAnalog(mConfiguration.recordingChannels[i]);
                        //float f=(float)frame.getAnalog(mConfiguration.activeChannels[i]);
                        Entry entry = new Entry(xValue, f);
                        graphs.get(i).addEntry(entry);
                    }
                }
            }
            samplingCounter -= samplingFrames;
        }
    }
    private boolean isViewVisible(View view) {
        Rect scrollBounds = new Rect();
        scrollView.getHitRect(scrollBounds);
        //float top = view.getY();
        //float bottom = top + view.getHeight();

        //if (scrollBounds.top <= top && scrollBounds.bottom >= bottom) {
        if(view.getLocalVisibleRect(scrollBounds)){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            mBound = true;
            connectToBitalino();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;

        }
    };
    public void connectToBitalino() {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Bundle b=new Bundle();
        b.putParcelable("Device",device);
        Message msg = Message.obtain(null, BitalinoCommunicationService.MSG_START_CONNECTION, 0, 0);
        msg.replyTo=activityMessenger;
        msg.setData(b);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void startRecording() {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Bundle b=new Bundle();
        b.putParcelable("Config",mConfiguration);
        Message msg = Message.obtain(null, BitalinoCommunicationService.MSG_START_RECORDING, 0, 0);
        msg.replyTo=activityMessenger;
        msg.setData(b);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void stopRecording() {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, BitalinoCommunicationService.MSG_STOP_RECORDING, 0, 0);
        msg.replyTo=activityMessenger;
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
            this.unbindService(mConnection);
            mBound=false;
            Intent intent=new Intent(this, BitalinoCommunicationService.class);
            stopService(intent);
            this.finish();
        }
    }
    /**
     * Permission check explicitly required from user at run time
     *
     * */

    private void permissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Android Marshmallow and above permission check
            if (this.checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.permission_check_dialog_title))
                        .setMessage(getString(R.string.permission_check_dialog_message))
                        .setPositiveButton(getString(R.string.permission_check_dialog_positive_button), null)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @TargetApi(Build.VERSION_CODES.M)
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 103);
                            }
                        });
                builder.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 103:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainMenuActivity", "Write external permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.permission_denied_dialog_title))
                            .setMessage(getString(R.string.permission_denied_dialog_message))
                            .setPositiveButton(getString(R.string.permission_denied_dialog_positive_button), null)
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {

                                }
                            });
                    builder.show();
                }
                break;
            default:
                return;
        }
    }

}
