package com.polito.cesarldm.tfg_bitadroidbeta;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;
import com.polito.cesarldm.tfg_bitadroidbeta.R;
import com.polito.cesarldm.tfg_bitadroidbeta.beats.Bdac;
import com.polito.cesarldm.tfg_bitadroidbeta.beats.SampleRate;
import com.polito.cesarldm.tfg_bitadroidbeta.services.BitalinoCommunicationService;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.ChannelConfiguration;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.DFTManager;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.FrameTransferFunction;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.MPAndroidGraph;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.SignalFilter;

import java.util.ArrayList;

import info.plux.pluxapi.bitalino.BITalinoFrame;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class HeartMonitorActivity extends AppCompatActivity implements View.OnClickListener{
    static final String TAG="SHOW DATA ACTIVITY";
    //UI
    Button btnStart, btnStop;
    SeekBar sbUpTh;
    ArrayList<BITalinoFrame> frames=new ArrayList<BITalinoFrame>();
    TextView bpm;
    Chronometer chrono;
    static SoundPool soundPool;
    static AudioManager amg;
    static int audio;
    //ArrayList<MPAndroidGraph> graphs=new ArrayList<MPAndroidGraph>();
    MPAndroidGraph mpAndroidGraph;
    MPAndroidGraph mpAndroidGraphBPM;
    //ListView graphList;
    FrameTransferFunction frameTransFunc;


    private double samplingFrames;
    private double samplingCounter = 0;
    private int updateBPMCounter=0;
    private long beatSampleCount=0;
    private long timeWhenStopped=0;
   static private double timeCounter = 0;
    static private float  xValueRatio;
    float xValue=0;
    private int numberOfFrames;
    private int delay;

    BluetoothDevice device;
    DFTManager mDftManager;
    static ChannelConfiguration mConfiguration;
    private int dataCheckCount=0;
    boolean mBound;
    boolean isVisible;
    boolean isConnected=false;
    private final Messenger activityMessenger = new Messenger(new HeartMonitorActivity.IncomingHandler());
    Messenger mService = null;
    private LayoutInflater inflater;
    public ProgressDialog progressDialogConnecting;
    private SignalFilter mSignalFilter;
    float sumForAvg=0;
    private float yMax,yMin,avg;
    private Bdac bdac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_monitor);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(!getIntent().hasExtra("Device")||!getIntent().hasExtra("Config")){
            Toast.makeText(this, "No device or config,", Toast.LENGTH_SHORT).show();
            finish();
        }
        inflater = this.getLayoutInflater();

        //Solicitar permisos
        permissionCheck();
        if(getIntent().getParcelableExtra("Device")!=null) {
            initSound();
            device = getIntent().getParcelableExtra("Device");
            mConfiguration = getIntent().getParcelableExtra("Config");
            btnStart = (Button) findViewById(R.id.btn_HM_start);
            btnStart.setOnClickListener(this);
            btnStop = (Button) findViewById(R.id.btn_HM_Stop);
            btnStop.setOnClickListener(this);
            Intent intent = new Intent(this, BitalinoCommunicationService.class);
            intent.putExtra("Device", device);
            intent.putExtra("Config", mConfiguration);
            bpm=(TextView)findViewById(R.id.tv_HM_bpm);
            chrono=(Chronometer)findViewById(R.id.chronometer2);
            samplingFrames = (double) mConfiguration.getSampleRate() / mConfiguration.getVisualizationRate();
            numberOfFrames = mConfiguration.getSampleRate();
            xValueRatio=mConfiguration.getVisualizationRate()/10;
            setActivityLayout();
            startService(intent);
            progressDialogConnecting=new ProgressDialog(HeartMonitorActivity.this);
            progressDialogConnecting.setMessage("Connecting to Bitalino");
            frameTransFunc=new FrameTransferFunction(mConfiguration);
            mDftManager=new DFTManager();
            bdac = new Bdac();
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
        mpAndroidGraph=new MPAndroidGraph(this,mConfiguration,0);
        RelativeLayout rl=(RelativeLayout)findViewById(R.id.relativeLayout_HM);
        mpAndroidGraph.getGraphView().setLayoutParams(layoutParams);
        rl.addView(mpAndroidGraph.getGraphView());
        mpAndroidGraphBPM=new MPAndroidGraph(this,mConfiguration,0);
        RelativeLayout rl2=(RelativeLayout)findViewById(R.id.rl_secondGraph);
        mpAndroidGraphBPM.getGraphView().setLayoutParams(layoutParams);
        rl2.addView(mpAndroidGraphBPM.getGraphView());



        /**ViewGroup.LayoutParams graphParams;
         View graphsView=findViewById(R.id.ll_SD);

         graphParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,250);

         for(int i=0; i<mConfiguration.getSize();i++){
         graphs.add(new MPAndroidGraph(this,mConfiguration.activeChannels[i],mConfiguration.activeChannelsNames[i]));
         LinearLayout graph = (LinearLayout) inflater.inflate(
         R.layout.in_ly_graph, null);
         //graphs.get(i).getGraphView().setOnTouchListener(graphTouchListener);
         graph.addView(graphs.get(i).getGraphView());
         ((ViewGroup) graphsView).addView(graph, graphParams);
         }**/
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
            case R.id.btn_HM_start:
                startRecording();
                startBeatDetection();
                chrono.setBase(SystemClock.elapsedRealtime()+timeWhenStopped);
                chrono.start();
                break;
            case R.id.btn_HM_Stop:
                stopRecording();
                timeWhenStopped=chrono.getBase()- SystemClock.elapsedRealtime();
                chrono.stop();

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
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void appendData(BITalinoFrame frame) {
        float f;
        int position=mConfiguration.recordingChannels[0];
        if (samplingCounter++ >= samplingFrames) {
            timeCounter++;
           xValue=xValueGenerator(timeCounter);
            /** if(mSignalFilter.checkFrame(frame.getAnalog(position))) {
             f = frame.getAnalog(position);
             }else{
             f=mSignalFilter.getAvg();
             }**/
            f = frame.getAnalog(position);
            if (isVisible) {
                    Entry entry = new Entry(xValue, f);
                    mpAndroidGraph.addEntry(entry);
                    samplingCounter -= samplingFrames;
                if (dataCheckCount >= mConfiguration.getSampleRate()*10) {
                    updateStatistics(xValue);
                    dataCheckCount = 0;
                    updateBPMCounter=0;
                }
            }
        }

    }
    private float xValueGenerator(double timeCounter) {
        float tempXValue = (float) (timeCounter* 1000) / mConfiguration.getVisualizationRate();
        return tempXValue;
    }



 public void updateStatistics(float xValue){
     long beats=updateBPMCounter*6;
     bpm.setText("Bpm: "+beats);
     Entry e=new Entry(xValue,(float) beats);
     mpAndroidGraphBPM.addEntry(e);

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
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                            }
                        });
                builder.show();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 2:
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
        toastMessageShort("Beat detection started");
        bdac.resetBdac();
        SampleRate.setSampleRate(mConfiguration.sampleRate);
        bdac.resetBdac();
        beatSampleCount = 0;


    }
    private void addSampletoBeatDetection(BITalinoFrame frame){
        int position=mConfiguration.recordingChannels[0];
        float f = frame.getAnalog(position);
        ++beatSampleCount;
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
        mpAndroidGraph.setHighLightt(x);
        updateBPMCounter++;
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
        soundPool.release();
        soundPool=null;

    }

}




