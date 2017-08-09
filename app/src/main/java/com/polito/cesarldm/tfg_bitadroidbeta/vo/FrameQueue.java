package com.polito.cesarldm.tfg_bitadroidbeta.vo;

import java.util.LinkedList;

import info.plux.pluxapi.bitalino.BITalinoFrame;

/**
 * Created by CesarLdM on 15/6/17.
 */

public class FrameQueue {
    private LinkedList list;

    public FrameQueue(){
        list= new LinkedList();

    }
    public FrameQueue(LinkedList list){
        this.list=list;
    }
    public float size(){
        float size=list.size();
        return size;
    }
    public boolean isEmpty(){return(list.size()==0);
    }
    public void enQueue(BITalinoFrame frame){
        list.add(frame);
    }
    public BITalinoFrame deQueue(){
        if(this.isEmpty()){
            return null;
        }
        BITalinoFrame frame= (BITalinoFrame) list.getFirst();
        list.removeFirst();
        return frame;
    }


    public void clear() {
        list.clear();
    }
}
