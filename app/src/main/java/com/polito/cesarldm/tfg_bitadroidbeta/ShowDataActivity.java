package com.polito.cesarldm.tfg_bitadroidbeta;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;
import com.polito.cesarldm.tfg_bitadroidbeta.services.*;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.ChannelConfiguration;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.FrameTransferFunction;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.Linechart;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.RecordingNotificationBuilder;



import java.util.ArrayList;

import info.plux.pluxapi.bitalino.BITalinoFrame;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ShowDataActivity extends AppCompatActivity  implements View.OnClickListener {

    static final String TAG="SHOW DATA ACTIVITY";
    //UI
    Button btnMap,btnZoomReset;
    RadioButton rdbtnRaw;
    ImageButton btnStart, btnStop,btnEnd,btnZoomIn,btnZoomOut;
    ArrayList<BITalinoFrame> frames=new ArrayList<BITalinoFrame>();
    ArrayList<Linechart> graphs=new ArrayList<Linechart>();
    ArrayList<Location> locations=new ArrayList<Location>();
    ScrollView scrollView;
    FrameTransferFunction frameTransFunc;
    private double timeCounter = 0;
    float xValue=0;
    private long timeWhenStopped=0;
    BluetoothDevice device;
    ChannelConfiguration mConfiguration;
    boolean mBound,isVisible,isRAWEnabled;
    boolean isConnected=false;
    boolean recordingStarted=false;
    private final Messenger activityMessenger = new Messenger(new IncomingHandler());
    Messenger mService = null;
    private LayoutInflater inflater;
    public ProgressDialog progressDialogConnecting;
    private AlertDialog alertDialogCheckEnd,alertDialogConnected;
    Chronometer chrono;
    RecordingNotificationBuilder mNotifierBuilder;

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
        setUpButtons();
        //Solicitar permisos
        permissionCheck();
        if(getIntent().getParcelableExtra("Device")!=null) {
            device = getIntent().getParcelableExtra("Device");
            mConfiguration = getIntent().getParcelableExtra("Config");
            chrono=(Chronometer)findViewById(R.id.chrono_SDA);
            scrollView=(ScrollView)findViewById(R.id.sc_SD);
            Intent intent = new Intent(this, BitalinoCommunicationService.class);
            setActivityLayout();
            startService(intent);
            alertDialogInitiate();
            progressDialogConnecting=new ProgressDialog(ShowDataActivity.this);
            progressDialogConnecting.setMessage("Connecting to Bitalino");
            frameTransFunc=new FrameTransferFunction(mConfiguration);
            mNotifierBuilder=new RecordingNotificationBuilder(this,1,getClass());

        }else {
            Toast.makeText(this, "No device selected ", Toast.LENGTH_SHORT).show();
            finish();
        }

    }
    private void setUpButtons(){
        btnStart = (ImageButton) findViewById(R.id.btn_SDA_start);
        btnStart.setOnClickListener(this);
        btnStop = (ImageButton) findViewById(R.id.btn_SDA_stop);
        btnStop.setOnClickListener(this);
        btnEnd = (ImageButton) findViewById(R.id.btn_SDA_end);
        btnEnd.setOnClickListener(this);
        btnMap=(Button) findViewById(R.id.bt_SDA_map);
        btnMap.setOnClickListener(this);
        btnZoomIn=(ImageButton)findViewById(R.id.btn_plus);
        btnZoomIn.setOnClickListener(this);
        btnZoomReset=(Button) findViewById(R.id.btn_reset);
        btnZoomReset.setOnClickListener(this);
        btnZoomOut=(ImageButton) findViewById(R.id.btn_minus);
        btnZoomOut.setOnClickListener(this);
        rdbtnRaw=(RadioButton)findViewById(R.id.RAW_btn);
        rdbtnRaw.setChecked(true);
        isRAWEnabled=true;
        rdbtnRaw.setOnClickListener(this);
    }


    private void alertDialogInitiate() {
        alertDialogCheckEnd=new AlertDialog.Builder(ShowDataActivity.this).create();
        alertDialogCheckEnd.setTitle(Html.fromHtml("<font color='#F44E42'>Warning</font>"));
        alertDialogCheckEnd.setMessage(Html.fromHtml("<font color='#F44E42'>Are you sure you want to stop the current recording?</font>"));
        alertDialogCheckEnd.setButton(Dialog.BUTTON_POSITIVE,Html.fromHtml("<font color='#F44E42'>YES</font>"),new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int which){
                mNotifierBuilder.closeNotification();
                endActivity();

            }
        });

        alertDialogCheckEnd.setButton(Dialog.BUTTON_NEGATIVE,Html.fromHtml("<font color='#F44E42'>NO</font>"),new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int which){
            }
        });
        alertDialogCheckEnd.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                alertDialogCheckEnd.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAlert));
                alertDialogCheckEnd.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorAlert));
            }
        });

        alertDialogCheckEnd.setIcon(R.drawable.ic_fail);
        alertDialogConnected=new AlertDialog.Builder(ShowDataActivity.this).create();
        alertDialogConnected.setTitle("Device Connected");
        alertDialogConnected.setMessage("Press Play to start recording");
        alertDialogConnected.setButton("OK",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int which){
            }
        });
        alertDialogConnected.setIcon(R.drawable.ic_check);

    }

    private void setActivityLayout() {
         float scale =getBaseContext().getResources().getDisplayMetrics().density;
        int pixels = (int) (150 * scale + 0.5f);
        //ScrollView sc=(ScrollView)findViewById(R.id.sc_SD);
        scrollView=(ScrollView)findViewById(R.id.sc_SD);
        LinearLayout ll=(LinearLayout)findViewById(R.id.ll_SD);
        //sc.addView(ll);
        LayoutParams graphParams,relativeParams;
       // View graphsView=findViewById(R.id.ll_SD);
        graphParams = new LayoutParams(LayoutParams.MATCH_PARENT,300);

        relativeParams=new LayoutParams(LayoutParams.MATCH_PARENT,pixels);
        for(int i=0; i<mConfiguration.getSize();i++){
            graphs.add(new Linechart(this,mConfiguration,i));
            //graphs.add(new MPAndroidGraph(this,mConfiguration,i));
                    //mConfiguration.activeChannels[i],mConfiguration.activeChannelsNames[i]));
            RelativeLayout graph = (RelativeLayout) inflater.inflate(
                    R.layout.graph_layout, null);
            ll.addView(graph);
            //graphs.get(i).getGraphView().setOnTouchListener(graphTouchListener);
            graph.addView(graphs.get(i).getGraphView(),relativeParams);
            //graphs.get(i).setLastzoomValue(50);
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
        for (int j = 0; j < graphs.size(); j++) {
            graphs.get(j).cleanPool();
        }
        super.onPause();
        isVisible=false;
    }
    @Override
    protected  void onDestroy(){
        super.onDestroy();
        if(mBound) {
            this.unbindService(mConnection);
        }
        for (int j = 0; j < graphs.size(); j++) {
            graphs.get(j).cleanPool();
            graphs.get(j).deleteChart();

        }
        Intent intent = new Intent(this, BitalinoCommunicationService.class);
        stopService(intent);
    }
    @Override
    public void onBackPressed(){
        if(recordingStarted) {
            alertDialogCheckEnd.show();

        }else{
            mNotifierBuilder.closeNotification();
            endActivity();
        }

    }
    private void endActivity() {
        Intent gpsIntentEnd=new Intent(this,GPSService.class);
        stopService(gpsIntentEnd);
        this.finish();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_SDA_start:
                startRecording();
                chrono.setBase(SystemClock.elapsedRealtime()+timeWhenStopped);
                chrono.start();
                Intent gpsIntent=new Intent(this,GPSService.class);
                startService(gpsIntent);
                mNotifierBuilder.launchNotification();
                break;
            case R.id.btn_SDA_stop:
                Toast.makeText(this,"F: "+graphs.get(0).getEntryCount(),Toast.LENGTH_LONG).show();
                stopRecording();
                Intent gpsIntentEnd=new Intent(this,GPSService.class);
                stopService(gpsIntentEnd);
                timeWhenStopped=chrono.getBase()- SystemClock.elapsedRealtime();
                chrono.stop();
                mNotifierBuilder.closeNotification();

                break;
            case R.id.btn_SDA_end:
                if(recordingStarted) {
                    alertDialogCheckEnd.show();

                }else {
                    mNotifierBuilder.closeNotification();
                    endActivity();
                }
                break;
            case R.id.bt_SDA_map:
                Intent iMap=new Intent (this,PopMapActivity.class);
                if(locations!=null) {
                    iMap.putParcelableArrayListExtra("Locations", locations);
                }
                startActivity(iMap);
                break;
            case R.id.btn_plus:
                for (int j = 0; j < graphs.size(); j++) {
                    graphs.get(j).zoomIn();
                }
                break;
            case R.id.btn_minus:
                for (int j = 0; j < graphs.size(); j++) {
                    graphs.get(j).zoomOut();
                }
                break;
            case R.id.btn_reset:
                for (int j = 0; j < graphs.size(); j++) {
                    graphs.get(j).resetZoom();
                }
                break;
            case R.id.RAW_btn:
                if(isRAWEnabled){
                    rdbtnRaw.setChecked(false);
                    isRAWEnabled=false;
                }else if(!isRAWEnabled){
                    rdbtnRaw.setChecked(true);
                    isRAWEnabled=true;
                }
                break;
        }
    }

    class IncomingHandler extends Handler {

                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case BitalinoCommunicationService.MSG_SEND_FRAME:
                            Bundle bf = msg.getData();
                            BITalinoFrame frames = bf.getParcelable("Frame");
                            appendData(frames);
                            recordingStarted = true;
                            break;

                        case BitalinoCommunicationService.MSG_SEND_CONNECTION_OFF:
                            Toast.makeText(getApplicationContext(), "Device Disconnected", Toast.LENGTH_SHORT).show();
                            isConnected = false;
                            break;

                        case BitalinoCommunicationService.MSG_ERROR:
                            switch (msg.arg1) {
                                case BitalinoCommunicationService.CODE_ERROR_TXT:
                                    Log.d(TAG, "TXT_ERROR");
                                    break;
                                case BitalinoCommunicationService.CODE_ERROR_SAVING:
                                    Log.d(TAG, "SAVING_ERROR");
                            }
                            break;

                        case BitalinoCommunicationService.MSG_SEND_CONNECTION_ON:
                            progressDialogConnecting.dismiss();
                            if (!recordingStarted) {
                                alertDialogConnected.show();
                            }
                            isConnected = true;
                            break;

                        case BitalinoCommunicationService.MSG_SEND_LOCATION:
                            Bundle bl = msg.getData();
                            Location location = bl.getParcelable("Location");
                            locations.add(location);
                            break;
                        default:
                            super.handleMessage(msg);
                    }
                }
        }
    private void appendData(BITalinoFrame frame) {// calculates x value of graphs
            timeCounter++;
        if (isVisible) {
            float f;
            float[] ftemp=new float[6];            xValue = (float) (timeCounter * 1000) / mConfiguration.getVisualizationRate();
            // gets default share preferences with multi-process flag
                for (int i = 0; i < graphs.size(); i++) {
                    if(isViewVisible(graphs.get(i).getGraphView())) {
                        if (!isRAWEnabled) {
                            ftemp = frameTransFunc.getConvertedValues(frame);
                            f = ftemp[i];
                        } else f = frame.getAnalog(mConfiguration.recordingChannels[i]);
                        graphs.get(i).addEntry(xValue, f);
                    }
                }
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
