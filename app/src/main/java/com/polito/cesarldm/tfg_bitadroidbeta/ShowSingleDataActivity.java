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
import android.graphics.Color;
import android.location.Location;
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
import com.polito.cesarldm.tfg_bitadroidbeta.services.BitalinoCommunicationService;
import com.polito.cesarldm.tfg_bitadroidbeta.services.GPSService;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.ChannelConfiguration;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.FrameTransferFunction;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.Linechart;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.LowPassFilter;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.RecordingNotificationBuilder;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.SignalFilter;

import java.math.BigDecimal;
import java.util.ArrayList;

import info.plux.pluxapi.bitalino.BITalinoFrame;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ShowSingleDataActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener,View.OnLongClickListener {
    static final String TAG="SHOW DATA ACTIVITY";
    //UI
    ImageButton btnStart, btnStop,btnEnd,btnZoomIn,btnZoomOut;
    Button btnMap,btnZoomReset;
    RadioButton rdbtnRaw;
    SeekBar sbUpTh;
    ArrayList<Location> locations=new ArrayList<Location>();
    TextView tvMax,tvMin,tvAvg,tvSel,tvSbVal,tvLoc;
    Linechart linechart;
    float outPut;
    //ListView graphList;
    FrameTransferFunction frameTransFunc;
    RecordingNotificationBuilder mNotifierBuilder;
    boolean recordingStarted=false;
    private double timeCounter = 0;
    float xValue=0;
    private long timeWhenStopped=0;
    BluetoothDevice device;
    ChannelConfiguration mConfiguration;
    private int dataCheckCount=0;
    boolean mBound;
    boolean isVisible;
    boolean isConnected=false;
    boolean isRAWEnabled;
    private final Messenger activityMessenger = new Messenger(new ShowSingleDataActivity.IncomingHandler());
    Messenger mService = null;
    public ProgressDialog progressDialogConnecting;
    private LowPassFilter mLowPasFilter;
    float sumForAvg=0;
    private float yMax,yMin,avg;
    private AlertDialog alertDialogCheckEnd, alertDialogConnected;
    Chronometer chrono;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_single_data);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(!getIntent().hasExtra("Device")||!getIntent().hasExtra("Config")){
            Toast.makeText(this, "No device or config,", Toast.LENGTH_SHORT).show();
            finish();
        }
        //Solicitar permisos
        permissionCheck();
        if(getIntent().getParcelableExtra("Device")!=null) {
            device = getIntent().getParcelableExtra("Device");
            mConfiguration = getIntent().getParcelableExtra("Config");
            setUpButtons();
            Intent intent = new Intent(this, BitalinoCommunicationService.class);
            intent.putExtra("Device", device);
            intent.putExtra("Config", mConfiguration);
            chrono=(Chronometer)findViewById(R.id.chrono_SSDA);
            setActivityLayout();
            startService(intent);
            alertDialogInitiate();
            progressDialogConnecting=new ProgressDialog(ShowSingleDataActivity.this);
            progressDialogConnecting.setMessage(getText(R.string.Connecting_progress));
            frameTransFunc=new FrameTransferFunction(mConfiguration);
            mNotifierBuilder=new RecordingNotificationBuilder(this,2,getClass());

        }else {
            Toast.makeText(this, "No device selected ", Toast.LENGTH_SHORT).show();
            finish();
        }

    }
    private void setUpButtons(){
        tvMax=(TextView)findViewById(R.id.tv_SSDA_maxY);
        tvMin=(TextView)findViewById(R.id.tv_SSDA_minY);
        tvAvg=(TextView)findViewById(R.id.tv_SSDA_avg);
        tvSel=(TextView)findViewById(R.id.tv_SSDA_selected);
        tvSbVal=(TextView)findViewById(R.id.tv_SSDA_sb_value);
        btnStart =(ImageButton) findViewById(R.id.btn_SSDA_start);
        btnStart.setOnClickListener(this);
        btnEnd =(ImageButton) findViewById(R.id.btn_SSDA_end);
        btnEnd.setOnClickListener(this);
        btnStop =(ImageButton) findViewById(R.id.btn_SSDA_stop);
        btnStop.setOnClickListener(this);
        btnMap=(Button) findViewById(R.id.bt_SSDA_map);
        btnMap.setOnClickListener(this);
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

    }
    private void setActivityLayout() {
        ViewGroup.LayoutParams layoutParams=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        linechart=new Linechart(this,mConfiguration,0);
        RelativeLayout rl=(RelativeLayout)findViewById(R.id.relativeLayout_SSDA);
        rl.setClickable(true);
        rl.setLongClickable(true);
        rl.setOnLongClickListener(this);
        rl.setOnClickListener(this);
        linechart.getGraphView().setLayoutParams(layoutParams);
        rl.addView(linechart.getGraphView());
        mLowPasFilter=new LowPassFilter();
        sbUpTh=(SeekBar)findViewById(R.id.sb_SSDA_aboveTH);
        sbUpTh.setOnSeekBarChangeListener(this);
        sbUpTh.setMax(100);
    }
    private void alertDialogInitiate() {
        alertDialogCheckEnd=new AlertDialog.Builder(ShowSingleDataActivity.this).create();
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

        alertDialogConnected=new AlertDialog.Builder(ShowSingleDataActivity.this).create();
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
        try {
            linechart.cleanPool();
            linechart.deleteChart();

        }catch (NullPointerException e){
            Log.d(TAG, "onDestroy: "+e.toString());
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
            case R.id.btn_SSDA_start:
                startRecording();
                chrono.setBase(SystemClock.elapsedRealtime()+timeWhenStopped);
                chrono.start();
                Intent gpsIntent=new Intent(this,GPSService.class);
                startService(gpsIntent);
                mNotifierBuilder.launchNotification();
                break;
            case R.id.btn_SSDA_stop:
                stopRecording();
                Intent gpsIntentEnd=new Intent(this,GPSService.class);
                stopService(gpsIntentEnd);
                timeWhenStopped=chrono.getBase()- SystemClock.elapsedRealtime();
                chrono.stop();
                mNotifierBuilder.closeNotification();
                break;
            case R.id.btn_SSDA_end:
                if(recordingStarted) {
                    alertDialogCheckEnd.show();

                }else{
                    mNotifierBuilder.closeNotification();
                    endActivity();
                }

                break;
            case R.id.bt_SSDA_map:
                Intent iMap=new Intent (this,PopMapActivity.class);
                if(locations!=null) {
                    iMap.putParcelableArrayListExtra("Locations", locations);
                }
                startActivity(iMap);
                break;
            case R.id.relativeLayout_SSDA:
                tvSel.setText("Y: "+Float.toString(linechart.getSelectedValue().getY()));
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
    @Override
    public boolean onLongClick(View v) {
        //linechart.saveAsImage();
        return false;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int value;
        if(progress<=15){
           value=0;
       }else if(progress<=33) {
           value=20;

       }else if(progress<=45) {
            value=40;

        }else if(progress<=65) {
            value=60;

        }else if(progress<=85) {
            value=80;

        }else if(progress<=95) {
            value=90;

        }else if(progress<=100){
            value=99;
        }else{
            value=0;
        }
        mLowPasFilter.updateAlpha(value);
        tvSbVal.setText(value+"%");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BitalinoCommunicationService.MSG_SEND_FRAME:
                    Bundle b =msg.getData();
                    final BITalinoFrame frame=b.getParcelable("Frame");
                    appendData(frame);
                    dataCheckCount++;
                    recordingStarted=true;
                    break;
                case BitalinoCommunicationService.MSG_SEND_CONNECTION_OFF:
                    Toast.makeText(getApplicationContext(),"Connection Ended",Toast.LENGTH_SHORT).show();
                    isConnected=false;
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
                    isConnected=true;
                    break;
                case BitalinoCommunicationService.MSG_SEND_LOCATION:
                    Bundle bl=msg.getData();
                    Location location=bl.getParcelable("Location");
                    locations.add(location);
                   // tvLoc.setText("Location: "+location.getLatitude()+" "+location.getLongitude());
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void appendData(BITalinoFrame frame) {
        timeCounter++;

            float[] ftemp = new float[6];
            float f;
            int position = mConfiguration.recordingChannels[0];
            xValue = (float) (timeCounter * 1000) / mConfiguration.getVisualizationRate();
            if(!isRAWEnabled){
                ftemp=frameTransFunc.getConvertedValues(frame);
                f=ftemp[0];
            }else f = frame.getAnalog(position);
            float tempOut = mLowPasFilter.lowPass(f, outPut);
            sumForAvg = sumForAvg + f;
            outPut = tempOut;
        if(isVisible) {
            linechart.addEntry(xValue, tempOut);
        }
            if (dataCheckCount >= mConfiguration.getVisualizationRate() / 2) {
                updateStatistics();
                dataCheckCount = 0;
            }
    }
    private void updateStatistics() {
        float size=linechart.getdataSize();
        yMax=round(linechart.getYMax(),2);
        yMin=round(linechart.getYMin(),2);
        avg=round(sumForAvg/size,2);
        tvMax.setText(getString(R.string.max)+yMax);
        tvMin.setText(getString(R.string.min)+yMin);
        tvAvg.setText(getString(R.string.avg)+avg);
        float y=round(linechart.getSelectedValue().getY(),2);
        tvSel.setText("Y: "+String.valueOf(y));
    }
    private static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
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
