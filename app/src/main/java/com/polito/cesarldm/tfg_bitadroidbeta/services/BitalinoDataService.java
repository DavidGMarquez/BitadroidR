package com.polito.cesarldm.tfg_bitadroidbeta.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.polito.cesarldm.tfg_bitadroidbeta.R;
import com.polito.cesarldm.tfg_bitadroidbeta.ShowDataActivity;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.ChannelConfiguration;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.DataManager;

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

public class BitalinoDataService extends Service {
    //Messages ints
    private BluetoothDevice device;
    private ChannelConfiguration mConfiguration;
    public static final int MSG_START_RECORDING =1;
    public static final int MSG_STOP_RECORDING=2;
    public static final int MSG_SEND_DATA=3;
    public static final int MSG_STOP=4;
    public static final int MSG_ERROR=5;
    public static final int MSG_SAVED=6;
    public static final int CODE_ERROR_TXT=7;
    public static final int CODE_ERROR_SAVING=8;
    public static final int MSG_CONNECTED=9;
    public static final int MSG_START_CONNECTION = 10;
    public static final int MSG_BOARD_STATE=11;
    public static final int MSG_SEND_STATE=12;
    public static final int MSG_SEND_DESC=13;

    //TODO Falta mantener ventana activa---->leer wakelock;
    static final  String TAG="Bita.Comm.service";
    boolean killServiceError = false;
    boolean isConnected=false;
    boolean isRecording=false;
    boolean connectedForRecording=false;
    boolean clientBinded=false;
    private final Messenger mMessenger = new Messenger(new IncomingHandler());
    private Messenger mClient;
    //Objects
    private BITalinoCommunication bitaCom;
    ArrayList<BITalinoFrame> frames=new ArrayList<BITalinoFrame>();

    private DataManager dataManager;
    Notification serviceNotification = null;

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_CONNECTION:
                    clientBinded=true;
                    device=msg.getData().getParcelable("Device");
                    mClient=msg.replyTo;
                    if(!isConnected) {
                        startConnection(device);
                    }

                case MSG_START_RECORDING:
                    if(isConnected) {
                        clientBinded=true;
                        connectedForRecording=true;
                        dataManager = new DataManager(getApplicationContext(), mConfiguration.getName(), mConfiguration,device);
                        createNotification();
                        startRecording();
                    }

                    break;
                case MSG_STOP_RECORDING:
                    connectedForRecording=true;
                    clientBinded=true;
                    if(isRecording) {
                        stopRecording();
                        isRecording=false;
                        if(isConnected) {
                            stopConnection();
                        }else {
                            sendStopMessage();
                            stopSelf();
                        }
                    }
                    break;
                case MSG_BOARD_STATE:
                    connectedForRecording=false;
                    clientBinded=true;
                    mClient=msg.replyTo;
                    if(isConnected) {
                        try {
                            bitaCom.state();
                        } catch (BITalinoException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                default:
                    super.handleMessage(msg);
            }
        }


    }

    @Override
    public void onCreate(){

        super.onCreate();

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
           Log.e(TAG,"Closing writers Error");
        }
        if(connectedForRecording) {
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
        try {
            if(isRecording) {
                bitaCom.stop();
            }
            if(isConnected) {
                bitaCom.disconnect();

            }
        } catch (BITalinoException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        try {
            if(isRecording) {
                bitaCom.stop();
            }
            if(isConnected) {
                bitaCom.disconnect();
            }
        } catch (BITalinoException e) {
            e.printStackTrace();
        }
        Log.d(TAG,"service unbinded");
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!intent.hasExtra("Device")||!intent.hasExtra("Config")){
            Toast.makeText(this, "No device or config,", Toast.LENGTH_SHORT).show();
            stopSelf();
        }
        Log.i(TAG,"Service Started");
        device=intent.getParcelableExtra("Device");
        mConfiguration=intent.getParcelableExtra("Config");
        initializeBitalinoApi();
        registerReceiver(updateReceiver, updateIntentFilter());

        return START_NOT_STICKY;
    }
    private void initializeBitalinoApi() {
        bitaCom = new BITalinoCommunicationFactory
                ().getCommunication(Communication.BTH, this.getApplicationContext(), new OnBITalinoDataAvailable(){
            @Override
            public void onBITalinoDataAvailable(BITalinoFrame bitalinoFrame) {
                Log.d(TAG, "BITalinoFrame: " + bitalinoFrame.toString());
                //descarto los frames vacios
                if(bitalinoFrame.getSequence()!=-1) {
                    //Part of code created by @author Carlos Marten Bitadroid APP BiopluxService.java
                    if (!dataManager.writeFrameToTmpFile(bitalinoFrame, bitalinoFrame.getSequence())) {
                        sendErrorToActivity(CODE_ERROR_TXT);
                        killServiceError = true;
                        stopSelf();
                    }
                        sendFrames(bitalinoFrame);

                }else {
                    Log.d(TAG, "FRAME VACIO--FRAME VACIO--FRAME VACIO--FRAME VACIO--FRAME VACIO--FRAME VACIO");
                }
            }
        });

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
                    BITalinoState bitaState=intent.getParcelableExtra(Constants.EXTRA_COMMAND_REPLY);
                    sendStateMessage(bitaState);
                    Log.d(TAG, "BITalinoState: " + parcelable.toString());

                } else if(parcelable.getClass().equals(BITalinoDescription.class)){
                    BITalinoDescription bitaDesc=intent.getParcelableExtra(Constants.EXTRA_COMMAND_REPLY);
                    sendDescriptionMessage(bitaDesc);
                    Log.d(TAG, "BITalinoDescription: isBITalino2: " +
                            ((BITalinoDescription)parcelable).isBITalino2() + "; FwVersion:"+
                            String.valueOf(((BITalinoDescription)parcelable).getFwVersion()));
                }
            } else if (Constants.ACTION_MESSAGE_SCAN.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(Constants.EXTRA_DEVICE_SCAN);
            }
        }
    };




    private void checkConnectionState(String state) {
        switch(state){
            case "CONNECTED":
                isConnected=true;
                sendConnectedMessage();


                break;
            case "ACQUISITION_OK":
                isRecording=true;


                break;
            case "DISCONNECTED":
                isConnected=false;
                stopConnection();
                sendStopMessage();
                break;


        }

    }

    protected static IntentFilter updateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_STATE_CHANGED);
        intentFilter.addAction(Constants.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(Constants.ACTION_COMMAND_REPLY);
        intentFilter.addAction(Constants.ACTION_MESSAGE_SCAN);
        return intentFilter;
    }

    public void startConnection(BluetoothDevice deviceConnfig){
        try {
            bitaCom.connect(deviceConnfig.getAddress());
        } catch (BITalinoException e) {
            e.printStackTrace();
        }
    }
    public void stopConnection(){
        try {
            bitaCom.disconnect();
        } catch (BITalinoException e) {
            e.printStackTrace();
        }
    }
    private void startRecording() {
        if(isConnected){
            try {
                bitaCom.start(mConfiguration.getActiveChannels(),mConfiguration.getSampleRate());
            } catch (BITalinoException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(this,"Device Not connected",Toast.LENGTH_LONG);

        }
    }
    private void stopRecording() {
        if(isConnected){
            try {
                bitaCom.stop();
            } catch (BITalinoException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(this,"Still recording",Toast.LENGTH_LONG);
        }
    }
    private void sendFrames( BITalinoFrame frame) {
        Bundle b=new Bundle();
            b.putParcelable("Frame", frame);
            Message message = Message.obtain(null, MSG_SEND_DATA,0,0);
            message.setData(b);
            try {
                mClient.send(message);
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
    }
    private void sendStateMessage(BITalinoState bitaState) {
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
       stopConnection();
    }
    private void sendDescriptionMessage(BITalinoDescription bitaDesc) {
        Bundle b=new Bundle();
        b.putParcelable("State", bitaDesc);
        Message message = Message.obtain(null, MSG_SEND_DESC,0,0);
        message.setData(b);
        try {
            mClient.send(message);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void sendStopMessage() {
        Bundle b=new Bundle();
        b.putBoolean("Stop",true);
        Message message=Message.obtain(null,MSG_STOP,0,0);
        message.setData(b);
        try {
            mClient.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        stopConnection();
        clientBinded=false;
    }
    private void sendConnectedMessage(){
        Bundle b=new Bundle();
        b.putBoolean("Connected",true);
        Message message=Message.obtain(null,MSG_CONNECTED,0,0);
        message.setData(b);
        try {
            mClient.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
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

    //--------------------------------------------------------------------------------------

}
