package com.polito.cesarldm.tfg_bitadroidbeta;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.SyncStateContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
 import com.polito.cesarldm.tfg_bitadroidbeta.adapters.DeviceListAdapter;
import com.polito.cesarldm.tfg_bitadroidbeta.services.BitalinoCommunicationService;
import com.polito.cesarldm.tfg_bitadroidbeta.services.BitalinoDataService;

import java.util.ArrayList;
import java.util.Set;

import info.plux.pluxapi.BTHDeviceScan;
import info.plux.pluxapi.Constants;
import info.plux.pluxapi.bitalino.BITalinoDescription;
import info.plux.pluxapi.bitalino.BITalinoFrame;
import info.plux.pluxapi.bitalino.BITalinoState;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class SelectDevicesActivity extends AppCompatActivity  implements View.OnClickListener, AdapterView.OnItemClickListener {

    //UI Elements
    FloatingActionButton btnRefresh;
    ListView lvNew;
    //Bluetooth
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private BTHDeviceScan bthDeviceScan;
    private BITalinoDescription desc;
    private BITalinoState state;
    //Adapters
    DeviceListAdapter  adapterNew;
    private int positionSelected;
    //boolean
    private boolean mScanning=false;
    private boolean mConnecting=false;
    private boolean isgood=false;
    private boolean mBound;
    Messenger mService = null;
    private final Messenger activityMessengerDevice = new Messenger(new IncomingHandler());
    private ProgressDialog prgDialogCheckBitalino;
    private AlertDialog alertDialogCheckBitalino;
    private AlertDialog alertDialogFailBitalino;

    private static final long SCAN_PERIOD = 5000;
    private static final long WAIT_PERIOD = 20000;
    private static final long CONFIRM_PERIOD = 4000;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BitalinoCommunicationService.MSG_SEND_DESC:
                    Bundle b1 = msg.getData();
                    desc = b1.getParcelable("Desc");
                    requestState();

                    break;
                case BitalinoCommunicationService.MSG_SEND_STATE:
                    Bundle b2 = msg.getData();
                    state = b2.getParcelable("State");
                    if(desc.isBITalino2()){
                        alertDialogCheckBitalino.show();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                alertDialogCheckBitalino.dismiss();
                                returnDeviceBitalinoDescription();

                            }
                        }, CONFIRM_PERIOD);
                        alertDialogCheckBitalino.show();

                    }
                    if(!desc.isBITalino2()){
                        alertDialogCheckBitalino.show();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                alertDialogCheckBitalino.dismiss();
                                returnDeviceBitalinoDescription();

                            }
                        }, CONFIRM_PERIOD);
                        alertDialogCheckBitalino.show();

                    }


                    break;
                case BitalinoCommunicationService.MSG_SEND_CONNECTION_ON:
                    prgDialogCheckBitalino.dismiss();
                    isgood=true;
                    requestDesc();


                    break;
                case BitalinoCommunicationService.MSG_SEND_CONNECTION_OFF:
                    prgDialogCheckBitalino.dismiss();
                    alertDialogFailBitalino.show();
                    toastMessageLong("Unable to connect to the selected device");
                    break;
                default:
                    super.handleMessage(msg);
                case BitalinoCommunicationService.MSG_SEND_NOTICE:
                    Bundle b3=msg.getData();
                    String s=b3.getString("Notice");
                    if(desc.isBITalino2()){
                        alertDialogCheckBitalino.show();
                    }
                    if (!desc.isBITalino2()) {
                            alertDialogCheckBitalino.show();
                        }

                    break;

            }
        }
    }

    private void returnDeviceBitalinoDescription() {
        try {
            final BluetoothDevice deviceNew = adapterNew.getItem(positionSelected);
            Intent returnIntentTwo = new Intent();
            returnIntentTwo.putExtra("result", deviceNew);
            returnIntentTwo.putExtra("Desc",desc);
            if(state!=null) {
                returnIntentTwo.putExtra("State", state);
            }
            overridePendingTransition(R.animator.lefttorigth,R.animator.rigthtoleft);
            setResult(SelectDevicesActivity.RESULT_OK, returnIntentTwo);
            this.finish();
        }catch(IndexOutOfBoundsException e){
        toastMessageLong("Something went wrong!, please try again");
        }

    }

    /**
     * ACTIVITY LIFE-CYCLE
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_devices);
        setUI();
        mHandler = new Handler();
        final BluetoothManager bthManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bthManager.getAdapter();
        alertDialogInitiate();
        permissionCheck();
        checkBlueToothState();

        //Intent intent = new Intent(this, BitalinoDataService.class);

    }

    private void alertDialogInitiate() {
        alertDialogCheckBitalino=new AlertDialog.Builder(SelectDevicesActivity.this).create();
        alertDialogCheckBitalino.setTitle("Selected Device");
        if(desc!=null){
            alertDialogCheckBitalino.setMessage("Bitalino device version "+desc.getFwVersion()+"active and in range");
        }else {
            alertDialogCheckBitalino.setMessage("Device active and in range, please wait...");
        }
       // alertDialogCheckBitalino.setButton("OK",new DialogInterface.OnClickListener(){
          //  public void onClick(DialogInterface dialog,int which){
               // returnDeviceBitalinoDescription();
          //  }
       // });
        alertDialogCheckBitalino.setIcon(R.drawable.ic_check);

        alertDialogFailBitalino=new AlertDialog.Builder(SelectDevicesActivity.this).create();
        alertDialogFailBitalino.setTitle("Selected Device");
        alertDialogFailBitalino.setMessage("Device not responding");
        alertDialogFailBitalino.setButton("OK",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int which){
                returnDeviceBitalinoDescription();
            }
        });
        alertDialogFailBitalino.setIcon(R.drawable.ic_fail);
    }

    @Override
    protected void onStart(){
        super.onStart();
        Intent intent = new Intent(this, BitalinoCommunicationService.class);
        startService(intent);
        registerReceiver(mBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        bthDeviceScan = new BTHDeviceScan(this);
        bindService(new Intent(this, BitalinoCommunicationService.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume(){
        super.onResume();
        adapterNew=new DeviceListAdapter(this);
        lvNew.setAdapter(adapterNew);
        startScanning();
    }
    @Override
    protected void onPause(){
        super.onPause();

    }
    @Override
    protected void onStop(){
        stopScanning();
        bthDeviceScan.closeScanReceiver();
        alertDialogCheckBitalino.dismiss();
        super.onStop();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        if(mBound){
            unbindService(mConnection);
        }
        Intent intent = new Intent(this, BitalinoCommunicationService.class);
        stopService(intent);
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.animator.lefttorigth,R.animator.rigthtoleft);
        this.finish();
    }
    /**
     * METHODS
     */
    private void setUI() {
        btnRefresh=(FloatingActionButton)findViewById(R.id.btn_SD_refresh);
        lvNew=(ListView)findViewById(R.id.lv_SD_new);
        btnRefresh.setOnClickListener(this);
        lvNew.setOnItemClickListener(this);
        prgDialogCheckBitalino=new ProgressDialog(SelectDevicesActivity.this);
        prgDialogCheckBitalino.setMessage("Checking selected device...");
    }
    private void stopScanning(){
        if (mScanning) {
            bthDeviceScan.stopScan();
        }

    }
    private void startScanning() {
        if (!mScanning) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bthDeviceScan.stopScan();
                }
            }, SCAN_PERIOD);
            mScanning = true;
            bthDeviceScan.doDiscovery();
        } else {
            mScanning = false;
            bthDeviceScan.stopScan();
        }
    }
    private void startBitalinoConnection(){
        if(!mConnecting) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!isgood) {
                        mConnecting = false;
                        stopConnectionToBitalino();
                    }
                }
            }, WAIT_PERIOD);
            mConnecting = true;
            connectToBitalino();
        }else {
            //mConnecting=false;
            //stopConnectionToBitalino();
        }

    }
    @Override
    public void onClick(View v) {
        adapterNew.clear();
        adapterNew.notifyDataSetChanged();
        lvNew.deferNotifyDataSetChanged();
        startScanning();

    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        prgDialogCheckBitalino.show();
        positionSelected=position;
       // startBitalinoConnection();
        connectToBitalino();

        }
    /**
     * BROADCAST RECIEVER
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice bthDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    adapterNew.addDevice(bthDevice);
                    adapterNew.notifyDataSetChanged();

            }
        }
    };
    /** BLUETOOTH VERIFICATION AND PERMISSION
     *
     *
     * */
    private void checkBlueToothState() {
        if (mBluetoothAdapter == null) {
            toastMessageLong("Error-Bluetooth not suported");
            return;
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                toastMessageShort("Bluetooth Enabled");
            } else {
                toastMessageShort("Bluetooth Disabled");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }

        }
    }
    /**
     * Permission check explicitly required from user at run time
     *
     * */

    private void permissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Android Marshmallow and above permission check
            if (this.checkSelfPermission(ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.permission_check_dialog_title))
                        .setMessage(getString(R.string.permission_check_dialog_message))
                        .setPositiveButton(getString(R.string.permission_check_dialog_positive_button), null)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @TargetApi(Build.VERSION_CODES.M)
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 102);
                            }
                        });
                builder.show();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 102:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainMenuActivity", "coarse location permission granted");
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
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };

    private void requestState() {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value

        Message msg = Message.obtain(null, BitalinoCommunicationService.MSG_RETURN_STATE, 0, 0);

        msg.replyTo=activityMessengerDevice;
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private void requestDesc() {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value

        Message msg = Message.obtain(null, BitalinoCommunicationService.MSG_RETURN_DESCRIPTION, 0, 0);

        msg.replyTo=activityMessengerDevice;
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }



    public void connectToBitalino() {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Bundle b=new Bundle();
        b.putParcelable("Device",adapterNew.getItem(positionSelected));
        Message msg = Message.obtain(null, BitalinoCommunicationService.MSG_START_CONNECTION, 0, 0);
        msg.replyTo=activityMessengerDevice;
        msg.setData(b);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void stopConnectionToBitalino() {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, BitalinoCommunicationService.MSG_STOP_CONNECTION, 0, 0);
        msg.replyTo=activityMessengerDevice;
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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

}
