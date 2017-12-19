package com.polito.cesarldm.tfg_bitadroidbeta.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;
import com.polito.cesarldm.tfg_bitadroidbeta.MainMenuActivity;
import com.polito.cesarldm.tfg_bitadroidbeta.R;
import com.polito.cesarldm.tfg_bitadroidbeta.ShowDataActivity;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.ChannelConfiguration;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.DataManager;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.SignalFilter;

import java.util.ArrayList;

import info.plux.pluxapi.Communication;
import info.plux.pluxapi.Constants;
import info.plux.pluxapi.bitalino.BITalinoCommunication;
import info.plux.pluxapi.bitalino.BITalinoCommunicationFactory;
import info.plux.pluxapi.bitalino.BITalinoDescription;
import info.plux.pluxapi.bitalino.BITalinoException;
import info.plux.pluxapi.bitalino.BITalinoFrame;
import info.plux.pluxapi.bitalino.BITalinoState;
import info.plux.pluxapi.bitalino.bth.OnBITalinoDataAvailable;

public class BitalinoCommunicationService extends Service {
    static final  String TAG="Bita.Comm.service";
    public static final int MSG_START_CONNECTION =1;
    public static final int MSG_STOP_CONNECTION=2;
    public static final int MSG_RETURN_DESCRIPTION=3;
    public static final int MSG_RETURN_STATE=4;
    public static final int MSG_START_RECORDING=5;
    public static final int MSG_STOP_RECORDING=6;
    public static final int CODE_ERROR_TXT=7;
    public static final int MSG_SEND_CONNECTION_ON=9;
    public static final int MSG_SEND_CONNECTION_OFF=10;
    public static final int MSG_SEND_NOTICE=11;
    public static final int MSG_SEND_STATE=12;
    public static final int MSG_SEND_DESC=13;
    public static final int MSG_SEND_FRAME=14;
    public static final int MSG_ERROR=15;
    public static final int MSG_SAVED=16;
    public static final int MSG_SEND_LOCATION=17;
    public static final int MSG_SEND_BPM_RR=18;


    private boolean isConnected=false;
    private boolean isRecording=false;
    private boolean isRecordMade=false;
    private boolean killServiceError=false;
    private Messenger mMessenger=new Messenger(new IncomingHandler());
    private Messenger mClient;
    private BITalinoCommunication bitaCom;
    private DataManager dataManager;
    private ChannelConfiguration mConfiguration;
    private BluetoothDevice device;
    private BITalinoDescription bitaDesc;
    private BITalinoState bitaState;
    private Notification serviceNotification=null;
    private PowerManager.WakeLock wakeLock=null;
    ArrayList<Entry> rrValues=new ArrayList<Entry>();
    ArrayList<Entry> bpmValues=new ArrayList<Entry>();
    NotificationManager nManager;
    private int freqMsg;
    private int frameCount=0;
    private int msgCount=0;
    private ArrayList<BITalinoFrame> framePack=new ArrayList<BITalinoFrame>();

    public static final int CODE_ERROR_SAVING=8;




    @Override
    public int onStartCommand(Intent intent, int flags,int startID){
        initializeBitalinoAPI();
        registerReceiver(updateReceiver,updateIntentFilter());
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();

        setNotifications();

        return START_NOT_STICKY;

    }

    private void setNotifications() {
        nManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        android.support.v4.app.NotificationCompat.Builder noticeActive=
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_play)
                        .setContentTitle("Bitadroid recording")
                        .setContentText("Active");
        Notification noti=new Notification(R.drawable.ic_play,"recording",System.currentTimeMillis());
        PendingIntent resultIntent=PendingIntent.getActivity(getApplicationContext(),0,new Intent(this, MainMenuActivity.class),0);


    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
        unregisterReceiver(updateReceiver);
        Log.d(TAG,"service ended");
        //Part of code created by @author Carlos Marten Bitadroid APP BiopluxService.java
        try {
            if (!dataManager.closeWriters()) {
                sendErrorToActivity(CODE_ERROR_SAVING);
            }
        }catch (NullPointerException e){

        }
            if(isRecordMade) {
                new Thread() {
                    @Override
                    public void run() {
                        boolean errorSavingRecording = false;
                        if (!dataManager.saveAndCompressFile(mClient)) {
                            errorSavingRecording = true;
                            sendErrorToActivity(CODE_ERROR_SAVING);
                        }
                        if (!errorSavingRecording)
                            sendSavedNotification();
                    }
                }.start();
            }
                //---------------------------------------------------------------------
                if (isRecording) {
                    stopRecording();
                }
                if (isConnected) {
                    stopConnection();
                }
        wakeLock.release();
    }

class IncomingHandler extends Handler {
    @Override
    public void handleMessage(Message msg){
        switch (msg.what){
            case MSG_START_CONNECTION:
                device=msg.getData().getParcelable("Device");
                mClient=msg.replyTo;
                if(!isConnected){
                    startConnection();
                }else{
                    sendConnectionON();
                }

                break;
            case MSG_STOP_CONNECTION:
                if(isConnected){
                    stopConnection();
                }else{
                    sendConnectionOFF();

                }

                break;
            case MSG_RETURN_DESCRIPTION:
                if(bitaDesc!=null) {
                    sendBitalinoDescription();
                }else{
                   Log.i(TAG,"Bitalino description Unavailable");
                }

                break;
            case MSG_RETURN_STATE:
                if(bitaState!=null) {
                    sendBitalinoState();
                }else{
                    requestState();
                    Log.i(TAG,"Bitalino state unavailable");
                    sendNoticeToUser("State not available");
                }

                break;
            case MSG_START_RECORDING:
                if(isConnected){
                    mConfiguration= msg.getData().getParcelable("Config");
                    if(!isRecording) {
                        startRecording();
                    }else{
                        Toast.makeText(getApplicationContext(),"Device already recording",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    sendConnectionOFF();
                }
                break;

            case MSG_STOP_RECORDING:
                if (isConnected) {
                    if (isRecording) {
                        stopRecording();

                    }else{
                        Toast.makeText(getApplicationContext(),"Device not recording",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    sendConnectionOFF();
                }
                break;
            case MSG_SEND_BPM_RR:
                float temprr,tempbpm;
                tempbpm=msg.getData().getFloat("bpm");
                temprr=msg.getData().getFloat("rr");
                if (!dataManager.writeBPMToTmpFile(temprr,tempbpm)) {
                    sendErrorToActivity(CODE_ERROR_TXT);
                    killServiceError = true;
                    stopSelf();
                }else {
                   Log.d(TAG,"BPMRR info sent to DATA MANAGER");
                }

                mClient=msg.replyTo;
                if(dataManager!=null){
                   // dataManager.addHMFiles(bpmValues,rrValues);
                }
                break;
            default:
                super.handleMessage(msg);
        }
    }

}
    private void startConnection() {
        try {
            bitaCom.connect(device.getAddress());
        } catch (BITalinoException e) {
            e.printStackTrace();
        }
    }
    private void stopConnection() {
        try {
            bitaCom.disconnect();
        } catch (BITalinoException e) {
            e.printStackTrace();
        }
    }
    private void requestState(){
        try {
            if(!bitaCom.state()){

            }
        } catch (BITalinoException e) {
            e.printStackTrace();
        }
    }

    private void startRecording() {
        if(mConfiguration.getVisualizationRate()>=100){
            freqMsg=10;
        }else{
            freqMsg=1;
        }
        dataManager=new DataManager(getApplicationContext(),mConfiguration.getName(),mConfiguration,device);
        createNotification();
        try {
            bitaCom.start(mConfiguration.getActiveChannels(),mConfiguration.getSampleRate());
        } catch (BITalinoException e) {
            e.printStackTrace();
        }
    }
    private void stopRecording() {
        try {
            bitaCom.stop();
            isRecording=false;
        } catch (BITalinoException e) {
            e.printStackTrace();
        }
    }

    private void sendBitalinoDescription(){
        Bundle b=new Bundle();
        b.putParcelable("Desc", bitaDesc);
        Message message = Message.obtain(null,MSG_SEND_DESC,0,0);
        message.setData(b);
        try {
            mClient.send(message);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
    private void sendNoticeToUser(String s){
        Bundle b=new Bundle();
        b.putString("Notice",s);
        Message message = Message.obtain(null, MSG_SEND_NOTICE,0,0);
        message.setData(b);
        try {
            mClient.send(message);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void sendBitalinoState() {
        Bundle b=new Bundle();
        b.putParcelable("State", bitaState);
        Message message = Message.obtain(null, MSG_SEND_STATE,0,0);
        message.setData(b);
        try {
            mClient.send(message);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
    private void sendConnectionON(){
        Message message = Message.obtain(null, MSG_SEND_CONNECTION_ON,0,0);
        try {
            mClient.send(message);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
    private void sendConnectionOFF(){
        Message message = Message.obtain(null, MSG_SEND_CONNECTION_OFF,0,0);
        try {
            mClient.send(message);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
    private void sendFrame(BITalinoFrame frame) {

        Bundle b = new Bundle();
        b.putParcelable("Frame",frame);
        Message message = Message.obtain(null, MSG_SEND_FRAME, 0, 0);
        message.setData(b);
        try {
            mClient.send(message);
            framePack.clear();
            msgCount=0;
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private void sendLocation( Location location) {
        Bundle b=new Bundle();
        b.putParcelable("Location", location);
        Message message = Message.obtain(null, MSG_SEND_LOCATION,0,0);
        message.setData(b);
        try {
            mClient.send(message);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Constants.ACTION_STATE_CHANGED.equals(action)) {
                String identifier = intent.getStringExtra(Constants.IDENTIFIER);
                Constants.States state =
                        Constants.States.getStates(intent.getIntExtra(Constants.EXTRA_STATE_CHANGED,0));
                Log.i(TAG, "Device " + identifier + ": " + state.name());
                checkConnectionState(state.name());
            } else if (Constants.ACTION_DATA_AVAILABLE.equals(action)) {
                BITalinoFrame frame = intent.getParcelableExtra(Constants.EXTRA_DATA);
                Log.d(TAG, "BITalinoFrame: " + frame.toString());

            } else if (Constants.ACTION_COMMAND_REPLY.equals(action)) {
                String identifier = intent.getStringExtra(Constants.IDENTIFIER);
                Parcelable parcelable = intent.getParcelableExtra(Constants.EXTRA_COMMAND_REPLY);

                if(parcelable.getClass().equals(BITalinoState.class)){
                    bitaState=intent.getParcelableExtra(Constants.EXTRA_COMMAND_REPLY);
                    sendBitalinoState();
                    Log.d(TAG, "BITalinoState: " + parcelable.toString());

                } else if(parcelable.getClass().equals(BITalinoDescription.class)){
                   bitaDesc=intent.getParcelableExtra(Constants.EXTRA_COMMAND_REPLY);
                    sendBitalinoDescription();
                    Log.d(TAG, "BITalinoDescription: isBITalino2: " +
                            ((BITalinoDescription)parcelable).isBITalino2() + "; FwVersion:"+
                            String.valueOf(((BITalinoDescription)parcelable).getFwVersion()));
                }
            } else if (Constants.ACTION_MESSAGE_SCAN.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(Constants.EXTRA_DEVICE_SCAN);
            }
            else if ("com.location.Broadcast".equals(action)){
                Location location=intent.getParcelableExtra("Location");
                if (!dataManager.writeLocationToTmpFile(location)) {
                    sendErrorToActivity(CODE_ERROR_TXT);
                    killServiceError = true;
                    stopSelf();
                }else {
                    sendLocation(location);
                }
                //enviar a archivo de localización

            }
        }
    };

    protected static IntentFilter updateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_STATE_CHANGED);
        intentFilter.addAction(Constants.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(Constants.ACTION_COMMAND_REPLY);
        intentFilter.addAction(Constants.ACTION_MESSAGE_SCAN);
        intentFilter.addAction("com.location.Broadcast");
        return intentFilter;
    }

    private void checkConnectionState(String state) {
        switch(state){
            case "CONNECTED":
                isConnected=true;
                sendConnectionON();


                break;
            case "ACQUISITION_OK":
                isRecording=true;
                isRecordMade=true;
                break;

            case "DISCONNECTED":
                if(isConnected){
                    isConnected=false;
                    isRecording=false;
                    sendConnectionOFF();
                    stopConnection();
                }
                break;
        }
    }

    private void initializeBitalinoAPI() {
        bitaCom = new BITalinoCommunicationFactory
                ().getCommunication(Communication.BTH, this.getApplicationContext(), new OnBITalinoDataAvailable(){
            @Override
            public void onBITalinoDataAvailable(BITalinoFrame bitalinoFrame) {
                if(bitalinoFrame.getSequence()!=-1) {
                processFrame(bitalinoFrame);
                }else {
                    Log.d(TAG, "EMPTY FRAME");
                }
            }
        });

    }

    private void processFrame(BITalinoFrame biTalinoFrame){
            if (!dataManager.writeFrameToTmpFile(biTalinoFrame, biTalinoFrame.getSequence())) {
                sendErrorToActivity(CODE_ERROR_TXT);
                killServiceError = true;
                stopSelf();
            }else {
                frameCount++;
                if (frameCount >= mConfiguration.getSampleRate() / mConfiguration.getVisualizationRate()) {
                    sendFrame(biTalinoFrame);
                        frameCount = 0;
                }
            }
    }

    //Methods created by @author Carlos Marten Bitadroid APP BiopluxService.java
    public void sendErrorToActivity(int code){
        try {
            mClient.send(Message
                    .obtain(null,MSG_ERROR,code,0));
        } catch (RemoteException e) {
            Log.e(TAG,
                    "Exception sending error message to activity. Service is stopping",
                    e);
        }

    }
    private void sendSavedNotification() {
        Message message = Message.obtain(null, MSG_SAVED);
        try {
            mClient.send(message);
        } catch (RemoteException e) {
            Log.e(TAG, "client is dead. Service is being stopped", e);
            killServiceError = true;
            stopSelf();
        }
    }

    private void createNotification() {

        // SET THE BASICS
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(R.drawable.notification)
                .setContentTitle(getString(R.string.bs_notification_title))
                .setContentText(getString(R.string.bs_notification_message));

        // CREATE THE INTENT CALLED WHEN NOTIFICATION IS PRESSED
        Intent newRecordingIntent = new Intent(this, ShowDataActivity.class);

        // PENDING INTENT
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                newRecordingIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(pendingIntent);

        // CREATES THE NOTIFICATION AND START SERVICE AS FOREGROUND
        serviceNotification = mBuilder.build();
    }

}
