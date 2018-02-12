package com.polito.cesarldm.tfg_bitadroidbeta;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
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
import android.widget.Toast;

import com.polito.cesarldm.tfg_bitadroidbeta.adapters.RVRecordingListAdapter;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.Constants;

import java.io.File;
import java.util.ArrayList;

public class SelectRecordingActivity extends AppCompatActivity implements View.OnClickListener {
    private String path= Environment.getExternalStorageDirectory()+Constants.APP_DIRECTORY;
    RVRecordingListAdapter rvAdapter;
    LinearLayoutManager mLinearLM;
    ArrayList<File> fileList=new ArrayList<File>();
    boolean isItemClicked=false;
    RecyclerView rvRecordListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_recording);
        rvRecordListView=(RecyclerView)findViewById(R.id.rv_recordings);
        mLinearLM = new LinearLayoutManager(this);
        rvAdapter= new RVRecordingListAdapter(this);
       //listAdapter=new RecordingListAdapter(this);
        rvRecordListView.setAdapter(rvAdapter);
        rvRecordListView.setLayoutManager(mLinearLM);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvRecordListView);
        listFiles();
        rvRecordListView.setOnClickListener(this);
        rvRecordListView.setMotionEventSplittingEnabled(false);

    }
    @Override
    protected void onResume(){
        super.onResume();
        isItemClicked=false;
        rvRecordListView.addOnItemTouchListener(new RecyclerTouchListener(this,rvRecordListView,new ClickListener() {
            @Override
            public void onClick(View view, int positionClicked) {
                if(!isItemClicked) {
                    isItemClicked = true;
                    File sendFile=rvAdapter.getSelectedCC(positionClicked);
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("text/plain");
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"email@example.com"});
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "My BITalino Recording");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "You can send your files, and display them using OpenSignals!" +"\n"+
                            " Enjoy");
                    Uri uri = Uri.fromFile(sendFile);
                    emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"));

                }

            }

            @Override
            public void onLongClick(View view, int position) {


            }
        }));

    }
    public void listFiles(){
        if(createPath()) {
            fileList.clear();
            File directory = new File(path);
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if ((!file.isDirectory()) && (file.getAbsolutePath().endsWith(".zip"))) {
                        fileList.add(file);
                    }
                }
                rvAdapter.setArray(fileList);
                rvAdapter.notifyDataSetChanged();
            }
        }
    }
    private boolean createPath(){
        File directory =new File(path);
        if(!directory.exists()){
            if(directory.mkdir()){
                return true;
            }else{
                return  false;
            }
        }else{
            return true;
        }
    }

    @Override
    public void onClick(View v) {

    }

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

                AlertDialog.Builder builder = new AlertDialog.Builder(SelectRecordingActivity.this); //alert for confirm to delete
                builder.setMessage(Html.fromHtml("<font color='#F44E42'>Are you sure to delete?</font>"));    //set message
                builder.setIcon(R.drawable.ic_fail);

                builder.setPositiveButton(Html.fromHtml("<font color='#F44E42'>REMOVE</font>"), new DialogInterface.OnClickListener() { //when click on DELETE
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       if(rvAdapter.getSelectedCC(position).delete()){
                           Toast.makeText(getApplicationContext(),"File deleted",Toast.LENGTH_SHORT).show();
                           rvAdapter.deleteREC(position);
                           listFiles();
                       }
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
