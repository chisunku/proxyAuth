package com.example.checking;

import java.util.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class Attendance_model {
    String email;
    Timestamp time;
    String timeRef;
    Date date;
    private int imgId;

    public Attendance_model(String email, Timestamp time, String timeRef, Date date, int imgId){
        this.email = email;
        this.time = time;
        this.timeRef = timeRef;
        this.date = date;
        this.imgId = imgId;
    }

    public void setEmail(String email){
        this.email = email;
    }
    public void setDate(Date date){
        this.date = date;
    }
    public void setTime(Timestamp time){
        this.time = time;
    }
    public void setTimeRef(String timeRef){
        this.timeRef = timeRef;
    }

    public String getEmail(){
        return email;
    }
    public Timestamp getTime(){
        return time;
    }
    public String getTimeRef(){
        return timeRef;
    }
    public Date getDate(){
        return date;
    }
    public int getImgId(){
        return imgId;
    }
}
