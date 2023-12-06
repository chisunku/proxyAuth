package com.example.checking;

import java.sql.Timestamp;

public class attendance_card_model {
    private Timestamp time;
    private String status;
    private String boxName;
    private int imgId;

    public attendance_card_model(String boxName, String status, Timestamp time, int imgId){
        this.imgId = imgId;
        this.boxName = boxName;
        this.status = status;
        this.time = time;
    }

    public String getStatus(){
        return status;
    }

    public Timestamp getTime(){
        return time;
    }

    public String getBoxName(){
        return boxName;
    }

    public int getImgId(){
        return imgId;
    }

    public void setImgId(int imgId){
        this.imgId = imgId;
    }

    public void setBoxName(String boxName){
        this.boxName = boxName;
    }

    public void setTime(Timestamp time){
        this.time = time;
    }

    public void setStatus(String status){
        this.status = status;
    }
}
