package com.polito.cesarldm.tfg_bitadroidbeta;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.polito.cesarldm.tfg_bitadroidbeta.adapters.RecordingListAdapter;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.Constants;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectRecordingActivity extends AppCompatActivity implements ListView.OnItemLongClickListener {
    private String path= Environment.getExternalStorageDirectory()+Constants.APP_DIRECTORY;
    private RecordingListAdapter listAdapter;
    ArrayList<File> fileList=new ArrayList<File>();

    //UI
    ListView recordListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_recording);
        recordListView=(ListView)findViewById(R.id.lv_SRA_recording_list);
        listAdapter=new RecordingListAdapter(this);
        recordListView.setAdapter(listAdapter);
        listFiles();
        recordListView.setOnItemLongClickListener(this);


    }

    public void listFiles(){

        if(createPath()) {
            File directory = new File(path);
            File[] files = directory.listFiles();
            for(File file:files){
                if((file.isDirectory()==false)&&(file.getAbsolutePath().endsWith(".zip"))){
                    fileList.add(file);
                }
            }
            listAdapter.setFileArray(fileList);
            listAdapter.notifyDataSetChanged();


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
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        File sendFile=listAdapter.getItem(position);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"email@example.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "subject here");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "body text");
        Uri uri = Uri.fromFile(sendFile);
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"));
        return true;
    }
}
