package com.polito.cesarldm.tfg_bitadroidbeta;

import android.Manifest;
import android.annotation.TargetApi;
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
import android.location.Location;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.jobs.MoveViewJob;
import com.polito.cesarldm.tfg_bitadroidbeta.beats.Bdac;
import com.polito.cesarldm.tfg_bitadroidbeta.beats.SampleRate;
import com.polito.cesarldm.tfg_bitadroidbeta.services.BitalinoCommunicationService;
import com.polito.cesarldm.tfg_bitadroidbeta.services.GPSService;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.ChannelConfiguration;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.FrameTransferFunction;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.HMFrame;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.Linechart;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.RecordingNotificationBuilder;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.SignalFilter;

import java.util.ArrayList;

import info.plux.pluxapi.bitalino.BITalinoFrame;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class HeartMonitorActivity extends AppCompatActivity implements View.OnClickListener{
    static final String TAG="SHOW DATA ACTIVITY";
    //UI
    ImageButton btnStart, btnStop,btnEnd,btnStats,btnZoomIn,btnZoomOut;
    Button btnMap,btnZoomReset;
    RadioButton rdbtnRaw;
    ArrayList<Entry> rrValues=new ArrayList<Entry>();
    ArrayList<Entry> bpmValues=new ArrayList<Entry>();
    ArrayList<Location> locations=new ArrayList<Location>();
    TextView tvBpm,tvRR;
    Chronometer chrono;
    static SoundPool soundPool;
    static AudioManager amg;
    static int audio;
    Linechart linechart;
    FrameTransferFunction frameTransFunc;
    HMFrame mHMFrame;
    private long beatSampleCount=0;
    private long RRSamplecount=0;
    private long timeWhenStopped=0;
   static private double timeCounter = 0;
    float xValue=0;
    private int delay;
    BluetoothDevice device;
    RecordingNotificationBuilder mNotifierBuilder;
    static ChannelConfiguration mConfiguration;
    private int dataCheckCount=0;
    boolean mBound;
    boolean isVisible;
    boolean isConnected=false;
    boolean recordingStarted=false;
    boolean isRAWEnabled;
    private final Messenger activityMessenger = new Messenger(new HeartMonitorActivity.IncomingHandler());
    Messenger mService = null;
    public ProgressDialog progressDialogConnecting;
    private Bdac bdac;
    private AlertDialog alertDialogCheckEnd, alertDialogConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_monitor);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(!getIntent().hasExtra("Device")||!getIntent().hasExtra("Config")){
            Toast.makeText(this, "No device or config,", Toast.LENGTH_SHORT).show();
            finish();
        }
        mHMFrame=new HMFrame();


        //Solicitar permisos
        permissionCheck();
        if(getIntent().getParcelableExtra("Device")!=null) {
            initSound();
            device = getIntent().getParcelableExtra("Device");
            mConfiguration = getIntent().getParcelableExtra("Config");
            btnStart = (ImageButton) findViewById(R.id.btn_HM_start);
            btnStart.setOnClickListener(this);
            btnStop = (ImageButton) findViewById(R.id.btn_HM_stop);
            btnStop.setOnClickListener(this);
            btnMap=(Button) findViewById(R.id.bt_HM_map);
            btnMap.setOnClickListener(this);
            btnStats=(ImageButton) findViewById(R.id.btn_HM_Stats);
            btnEnd = (ImageButton) findViewById(R.id.btn_HM_end);
            btnEnd.setOnClickListener(this);
            btnStats.setOnClickListener(this);
            btnZoomIn=(ImageButton)findViewById(R.id.btn_plus);
            btnZoomIn.setOnClickListener(this);
            btnZoomReset=(Button) findViewById(R.id.btn_reset);
            btnZoomReset.setOnClickListener(this);
            btnZoomOut=(ImageButton) findViewById(R.id.btn_minus);
            btnZoomOut.setOnClickListener(this);
            rdbtnRaw=(RadioButton)findViewById(R.id.RAW_btn);
            rdbtnRaw.setOnClickListener(this);
            rdbtnRaw.setChecked(true);
            isRAWEnabled=true;
            Intent intent = new Intent(this, BitalinoCommunicationService.class);
            intent.putExtra("Device", device);
            intent.putExtra("Config", mConfiguration);
            tvBpm=(TextView)findViewById(R.id.tv_HM_bpm);
            tvRR=(TextView)findViewById(R.id.tv_HM_rr);
            chrono=(Chronometer)findViewById(R.id.chrono_HM);
            setActivityLayout();
            startService(intent);
            alertDialogInitiate();
            progressDialogConnecting=new ProgressDialog(HeartMonitorActivity.this);
            progressDialogConnecting.setMessage("Connecting to Bitalino");
            frameTransFunc=new FrameTransferFunction(mConfiguration);
            bdac = new Bdac();
            mNotifierBuilder=new RecordingNotificationBuilder(this,3,getClass());
        }else {
            Toast.makeText(this, "No device selected ", Toast.LENGTH_SHORT).show();
            finish();
        }


    }

    private void initSound() {
        int maxStreams=1;
        Context mContext=getApplicationContext();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            soundPool=new SoundPool.Builder().setMaxStreams(maxStreams).build();
        }else {
            soundPool = new SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0);
        }
        audio=soundPool.load(mContext,R.raw.beep,1);
        amg=(AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
    }


    private void setActivityLayout() {

        ViewGroup.LayoutParams layoutParams=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
       // mpAndroidGraph=new MPAndroidGraph(this,mConfiguration,0);
        linechart=new Linechart(this,mConfiguration,0);
        RelativeLayout rl=(RelativeLayout)findViewById(R.id.relativeLayout_HM);
        linechart.getGraphView().setLayoutParams(layoutParams);
        rl.addView(linechart.getGraphView());

    }
    private void alertDialogInitiate() {
        alertDialogCheckEnd=new AlertDialog.Builder(HeartMonitorActivity.this).create();
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

        alertDialogConnected=new AlertDialog.Builder(HeartMonitorActivity.this).create();
        alertDialogConnected.setTitle("Device Connected");
        alertDialogConnected.setMessage("Press Play to start recording");
        alertDialogConnected.setButton("OK",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int which){
            }
        });
        alertDialogConnected.setIcon(R.drawable.ic_check);
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
    protected void onPause(){
        super.onPause();
        isVisible=false;
        linechart.cleanPool();
    }
    @Override
    protected void onStop() {
        super.onStop();

    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(mBound) {
            unbindService(mConnection);
        }
        linechart.cleanPool();
        linechart.deleteChart();
        Intent intent = new Intent(this, BitalinoCommunicationService.class);
        stopService(intent);
        cleanUpIfEnd();
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
            case R.id.btn_HM_start:
                startRecording();
                startBeatDetection();
                Intent gpsIntent=new Intent(this,GPSService.class);
                startService(gpsIntent);
                chrono.setBase(SystemClock.elapsedRealtime()+timeWhenStopped);
                chrono.start();
                mNotifierBuilder.launchNotification();
                break;
            case R.id.btn_HM_stop:
                stopRecording();
                Intent gpsIntentStop=new Intent(this,GPSService.class);
                stopService(gpsIntentStop);
                timeWhenStopped=chrono.getBase()- SystemClock.elapsedRealtime();
                chrono.stop();
                mNotifierBuilder.closeNotification();
                break;
            case R.id.btn_HM_end:
                if(recordingStarted) {
                    alertDialogCheckEnd.show();

                }else{
                    mNotifierBuilder.closeNotification();
                    endActivity();
                }
                break;
            case R.id.bt_HM_map:
                Intent iMap=new Intent (this,PopMapActivity.class);
                if(locations!=null) {
                    iMap.putParcelableArrayListExtra("Locations", locations);
                }
                startActivity(iMap);
                break;
            case R.id.btn_HM_Stats:
                if(bpmValues!=null&&rrValues!=null) {
                    Intent iStats = new Intent(this, PopBPMRRGraphActivity.class);
                    iStats.putParcelableArrayListExtra("bpm", mHMFrame.getBpmValues());
                    iStats.putParcelableArrayListExtra("rr", mHMFrame.getRrValues());
                    startActivity(iStats);
                }
                break;
            case R.id.btn_plus:
                linechart.zoomIn();

                break;
            case R.id.btn_minus:
                linechart.zoomOut();

                break;
            case R.id.btn_reset:
                linechart.resetZoom();

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
                    Bundle b =msg.getData();
                    BITalinoFrame frame=b.getParcelable("Frame");
                    addSampletoBeatDetection(frame);
                    appendData(frame);
                    dataCheckCount++;
                    recordingStarted=true;
                    break;
                case BitalinoCommunicationService.MSG_SEND_CONNECTION_OFF:
                    Toast.makeText(getApplicationContext(),"Connection ended",Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getApplicationContext(),"Connection ended",Toast.LENGTH_SHORT).show();
                    if(!recordingStarted){
                        alertDialogConnected.show();
                    }

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
        timeCounter++;
        if (isVisible) {
        float f;
        float[] ftemp= new float[6];
        int position=mConfiguration.recordingChannels[0];
        xValue=xValueGenerator(timeCounter);
        if(!isRAWEnabled){
            ftemp=frameTransFunc.getConvertedValues(frame);
            f=ftemp[0];
        }else f = frame.getAnalog(position);
                   linechart.addEntry(xValue,f);

            }
            if (dataCheckCount >= mConfiguration.getSampleRate()*10) {
                dataCheckCount = 0;

            }
    }
    private float xValueGenerator(double timeCounter) {
        float tempXValue = (float) (timeCounter* 1000) / mConfiguration.getVisualizationRate();
        return tempXValue;
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
    public void sendRRBpm(float tempRR,float tempbpm){
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Bundle b=new Bundle();
        b.putFloat("rr",tempRR);
        b.putFloat("bpm",tempbpm);
        Message msg = Message.obtain(null, BitalinoCommunicationService.MSG_SEND_BPM_RR, 0, 0);
        msg.replyTo=activityMessenger;
        msg.setData(b);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
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
    /**
     * Adaptacion de codigo de detccion de latidos "AÑADIR NOMBRE Y DIRECCION" para lectura a tiempo
     * real
     */
    private void startBeatDetection() {

        bdac.resetBdac();
        SampleRate.setSampleRate(mConfiguration.sampleRate);
        bdac.resetBdac();
        beatSampleCount = 0;
    }
    private void addSampletoBeatDetection(BITalinoFrame frame){
        int position=mConfiguration.recordingChannels[0];
        float f = frame.getAnalog(position);
        ++beatSampleCount;
        ++RRSamplecount;
        delay = bdac.beatDetect((int) f, (int) beatSampleCount);

        // If a beat was detected, annotate the beat location
        // and type.
        if (delay != 0) {
            long detectionTimeR = beatSampleCount - delay;
            addHighligths((int)detectionTimeR);
            //generarMarcas(signals.get(0).getSignal(), detectionTimeR,
                    //Color.GREEN);

        }
    }
    public void addHighligths(int time){
        playSound(audio);
       float x= xValueGenerator((double) time);
        linechart.setHighLightt(x);
        calculateRRTime();

    }

    private void calculateRRTime() {
        float tempRR=(float) RRSamplecount/mConfiguration.getSampleRate();
        tvRR.setText("RR. "+tempRR+"seg");
        float xValue=xValueGenerator(timeCounter);
        Entry e=new Entry(xValue,tempRR);
        rrValues.add(e);
        mHMFrame.addRR(e);
        float beats= 60/tempRR;
        tvBpm.setText("Bpm: "+beats);
        Entry e2=new Entry(xValue,beats);
        bpmValues.add(e2);
        mHMFrame.addBpm(e2);
        RRSamplecount=0;
        sendRRBpm(tempRR,beats);

    }

    /**  TOAST METHODS
     *
     */
    public void toastMessageShort(String a) {
        Toast.makeText(this, a, Toast.LENGTH_SHORT).show();

    }

    public void toastMessageLong(String a) {
        Toast.makeText(this, a, Toast.LENGTH_LONG).show();

    }
    static void playSound(int sound){
        soundPool.play(sound,1,1,1,0,1f);
    }

    public void cleanUpIfEnd(){
        audio=0;
        if(soundPool!=null){
            soundPool.release();
            soundPool=null;
        }

    }

}




