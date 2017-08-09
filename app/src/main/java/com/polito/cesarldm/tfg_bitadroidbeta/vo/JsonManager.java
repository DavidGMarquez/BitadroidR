package com.polito.cesarldm.tfg_bitadroidbeta.vo;

import android.content.Context;
import android.os.Environment;
import android.provider.MediaStore;
import android.renderscript.Type;
import android.util.JsonWriter;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import info.plux.pluxapi.*;

/**
 * Created by CesarLdM on 19/6/17.
 */

public class JsonManager {
    private static String TAG="JsonMnager";
    private static String fileName="ChannelconfigBitadroid.txt";
    private static String jString;
    private static String path= Environment.getExternalStorageDirectory()+Constants.APP_DIRECTORY+"/ChannelConfig/";
    private ChannelConfiguration mConf;
    List<ChannelConfiguration> configList;
    Gson gson;
    private FileOutputStream outputStream;
    OutputStreamWriter mOutWriter;
    Context context;




    public JsonManager(){}



    public JsonManager(Context context,ChannelConfiguration mChannelConf) {
        this.gson = new Gson();
        this.mConf = mChannelConf;
        this.context = context;
        List<ChannelConfiguration> templist = this.getCurrentChannelConfigs();
        if (templist == null) {
            templist=new ArrayList<ChannelConfiguration>();
            templist.add(mChannelConf);
        }else{
            templist.add(mChannelConf);
        }
        updateChannelConfigFile(templist);

        }

    private void updateChannelConfigFile(List<ChannelConfiguration> tempList) {
        String jString = gson.toJson(tempList);
        if (createPath()) {
            try {
                File f = new File(path + fileName);
                f.createNewFile();
                FileOutputStream fOut = new FileOutputStream(f,false);
                fOut.write(jString.getBytes());
                fOut.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }


    private boolean createPath(){
        File directory =new File(path);
        if(!directory.exists()){
            if(directory.mkdir()){
                Log.d(TAG,"directory created");
                return true;
            }else{
                return  false;
            }
            }else{
            return true;
        }
        }

        public List<ChannelConfiguration> getCurrentChannelConfigs(){

           Gson gson=new GsonBuilder().create();
            File f =new File(path+fileName);
            if(f.exists()) {
                try {
                    BufferedReader br = new BufferedReader(new FileReader(path + fileName));
                 configList=gson.fromJson(br,new TypeToken<List<ChannelConfiguration>>(){}.getType());
                  // ChannelConfiguration tempconfigList = gson.fromJson(br,ChannelConfiguration.class);
                   // configList.add(tempconfigList);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return configList;
        }
        public void deleteConfiguration(int position){
            this.gson=new Gson();
            List<ChannelConfiguration> tempList = this.getCurrentChannelConfigs();
            tempList.remove(position);
            Log.d(TAG, "deleteConfiguration: configuration deleted");
            updateChannelConfigFile(tempList);
        }





    }





