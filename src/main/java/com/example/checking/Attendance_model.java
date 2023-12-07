package com.example.checking;

import java.io.Serializable;
import java.util.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class Attendance_model implements Serializable {
    String email;
    String time;
    String timeRef;
    String date;
    private int imgId;

    public Attendance_model(){

    }

    public Attendance_model(String email, String time, String timeRef, String date, int imgId){
        this.email = email;
        this.time = time;
        this.timeRef = timeRef;
        this.date = date;
        this.imgId = imgId;
    }

    public void setEmail(String email){
        this.email = email;
    }
    public void setDate(String date){
        this.date = date;
    }
    public void setTime(String time){
        this.time = time;
    }
    public void setTimeRef(String timeRef){
        this.timeRef = timeRef;
    }

    public String getEmail(){
        return email;
    }
    public String getTime(){
        return time;
    }
    public String getTimeRef(){
        return timeRef;
    }
    public String getDate(){
        return date;
    }
    public int getImgId(){
        return imgId;
    }
}
