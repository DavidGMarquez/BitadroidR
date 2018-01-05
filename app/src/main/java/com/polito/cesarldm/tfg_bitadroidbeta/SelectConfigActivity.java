package com.polito.cesarldm.tfg_bitadroidbeta;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.polito.cesarldm.tfg_bitadroidbeta.adapters.ConfigListAdapter;
import com.polito.cesarldm.tfg_bitadroidbeta.adapters.RVConfigListAdapter;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.ChannelConfiguration;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.JsonManager;

import java.util.ArrayList;
import java.util.List;

public class SelectConfigActivity extends AppCompatActivity implements View.OnClickListener{
    //UI
    FloatingActionButton btnNewConf;
    TextView textView;

    //ListView configList;
    boolean isItemClicked=false;
    boolean isItemLongClicked=false;
    RecyclerView rvConfigList;
    RVConfigListAdapter rvAdapter;
    LinearLayoutManager mLinearLM;
    JsonManager jsonManager;
    ConfigListAdapter adapter;
    BluetoothDevice device;
    List<ChannelConfiguration> returnedList;
    ArrayList<ChannelConfiguration> channelConfList = new ArrayList<ChannelConfiguration>();
    int longPressedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().hasExtra("Device")) {
            device = getIntent().getParcelableExtra("Device");
        } else {
            device = null;
        }
        setContentView(R.layout.activity_select_config);
        textView=(TextView)findViewById(R.id.tv_SC_list);
        textView.setOnClickListener(this);
        rvConfigList = (RecyclerView) findViewById(R.id.rv);
        mLinearLM = new LinearLayoutManager(this);
        rvAdapter = new RVConfigListAdapter(this);
        rvConfigList.setAdapter(rvAdapter);
        rvConfigList.setLayoutManager(mLinearLM);
        rvConfigList.setOnClickListener(this);

        //configList=(ListView)findViewById(R.id.lv_SC);
        btnNewConf = (FloatingActionButton) findViewById(R.id.btn_SC_new_conf);
        jsonManager = new JsonManager();
        btnNewConf.setOnClickListener(this);
        //adapter=new ConfigListAdapter(this);

        //configList.setAdapter(adapter);
        //configList.setOnItemClickListener(this);
        //configList.setOnItemLongClickListener(this);
        rvConfigList.setOnClickListener(this);
        rvConfigList.setMotionEventSplittingEnabled(false);
        listConfigurations();

    }


    @Override
    protected void onStart() {
        super.onStart();
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvConfigList);

    }

    @Override
    protected void onResume() {
        super.onResume();
        listConfigurations();
        isItemClicked=false;
        isItemLongClicked=false;
        rvConfigList.addOnItemTouchListener(new RecyclerTouchListener(this, rvConfigList, new ClickListener() {
            @Override
            public void onClick(View view, int positionClicked) {
                if(!isItemClicked) {
                    isItemClicked = true;
                    ChannelConfiguration selectedChannelConfig = rvAdapter.getSelectedCC(positionClicked);
                    if (selectedChannelConfig.getRecordingChannels().length == 1) {
                        Intent intentStartSingle = new Intent(getApplicationContext(), ShowSingleDataActivity.class);
                        intentStartSingle.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intentStartSingle.putExtra("Device", device);
                        intentStartSingle.putExtra("Config", selectedChannelConfig);
                        startActivity(intentStartSingle);
                    } else {
                        Intent intentStart = new Intent(getApplicationContext(), ShowDataActivity.class);
                        intentStart.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intentStart.putExtra("Device", device);
                        intentStart.putExtra("Config", selectedChannelConfig);
                        startActivity(intentStart);
                    }
                }

            }
            @Override
            public void onLongClick(View view, int position) {
                if(!isItemLongClicked) {
                    isItemLongClicked = true;
                    longPressedPosition=position;
                    ChannelConfiguration selectedChannelConfig = rvAdapter.getSelectedCC(position);
                        Intent intentStartSingle = new Intent(getApplicationContext(), CreateConfigActivity.class);
                        intentStartSingle.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intentStartSingle.putExtra("Config", selectedChannelConfig);
                        startActivityForResult(intentStartSingle,2);

                }

            }
        }));


    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onClick(View v) {
        Intent bthIntent = new Intent(this, CreateConfigActivity.class);
        startActivityForResult(bthIntent, 1);
    }





    public void listConfigurations() {
        returnedList = jsonManager.getCurrentChannelConfigs();
        if (returnedList == null||returnedList.size()==0){
            textView.setText("No configuration found, add new");
        }else {
            channelConfList.clear();
            channelConfList.addAll(returnedList);
            rvAdapter.setArray(channelConfList);
            rvAdapter.notifyDataSetChanged();
            //adapter.setConfigArray(channelConfList);
            // adapter.notifyDataSetChanged();
            textView.setText("Select configuration");
        }

    }

    /**
     * RECEIVES THE CONFIGURATION CREATED BY THE USER
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == SelectDevicesActivity.RESULT_OK) {
                Bundle b = data.getExtras();
                ChannelConfiguration mChannelConfig = b.getParcelable("result");
                rvAdapter.addConfiguration(mChannelConfig);
                rvAdapter.notifyDataSetChanged();


            }
            if (resultCode == SelectDevicesActivity.RESULT_CANCELED) {

                toastMessageShort("No configuration created");
            }

        }if(requestCode==2){
            if (resultCode == SelectDevicesActivity.RESULT_OK) {
                Bundle b = data.getExtras();
                ChannelConfiguration mChannelConfig = b.getParcelable("result");
                toastMessageLong("Configuration Updated");
                jsonManager.updateConfig(longPressedPosition,mChannelConfig);
                rvAdapter.updateConfiguration(longPressedPosition,mChannelConfig);
                rvAdapter.notifyDataSetChanged();

            }
            if (resultCode == SelectDevicesActivity.RESULT_CANCELED) {

                toastMessageShort("No changes added");
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



    /**
     * @Override public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
     * ChannelConfiguration selectedChannelConfig=adapter.getItem(position);
     * jsonManager.deleteConfiguration(position);
     * listConfigurations();
     * <p>
     * return true;
     * }
     **/

    private interface ClickListener{
         void onClick(View view,int position);
         void onLongClick(View view,int position);
    }
    private class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        private RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener){

            this.clicklistener=clicklistener;
            this.gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child=recycleView.findChildViewUnder(e.getX(),e.getY());
                    if(child!=null && clicklistener!=null){
                        clicklistener.onLongClick(child,recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child=rv.findChildViewUnder(e.getX(),e.getY());
            if(child!=null && clicklistener!=null && gestureDetector.onTouchEvent(e)){
                clicklistener.onClick(child,rv.getChildAdapterPosition(child));
            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

            return false;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition(); //get position which is swipe
            if(position==-1)return;
            if (direction == ItemTouchHelper.LEFT) {    //if swipe left

                AlertDialog.Builder builder = new AlertDialog.Builder(SelectConfigActivity.this); //alert for confirm to delete
                builder.setMessage(Html.fromHtml("<font color='#F44E42'>Are you sure to delete?</font>"));    //set message
                builder.setIcon(R.drawable.ic_fail);
                builder.setNegativeButton(Html.fromHtml("<font color='#F44E42'>REMOVE</font>"), new DialogInterface.OnClickListener() { //when click on DELETE
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        jsonManager.deleteConfiguration(position);
                        rvAdapter.deleteConfig(position);
                        listConfigurations();
                        return;
                    }
                }).setNegativeButton(Html.fromHtml("<font color='#F44E42'>CANCEL</font>"), new DialogInterface.OnClickListener() {  //not removing items if cancel is done
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        rvAdapter.notifyItemRemoved(position + 1);    //notifies the RecyclerView Adapter that data in adapter has been removed at a particular position.
                        rvAdapter.notifyItemRangeChanged(position, rvAdapter.getItemCount());   //notifies the RecyclerView Adapter that positions of element in adapter has been changed from position(removed element index to end of list), please update it.
                        return;
                    }
                }).show();  //show alert dialog
            }
        }
    };

}
