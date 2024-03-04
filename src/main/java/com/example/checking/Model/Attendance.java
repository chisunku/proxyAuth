package com.example.checking.Model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Attendance implements Serializable {
    String email;
    Date checkInDate;
    Date checkOutDate;
    private String img;
    private LocationsModel location;
    private Date date;

    public Attendance(){

    }

    public Attendance(String email, Date checkInDate, Date checkOutDate, String img, LocationsModel location, Date date){
        this.email = email;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.img = img;
        this.location = location;
        this.date = date;
    }

    public void setEmail(String email){
        this.email = email;
    }
    public void setCheckInDate(Date checkInDate){
        this.checkInDate = checkInDate;
    }
    public void setCheckOutDate(Date checkOutDate){
        this.checkOutDate = checkOutDate;
    }
    public void setImg(String img){
        this.img = img;
    }
    public void setLocationsModel(LocationsModel locationsModel){this.location = locationsModel;}
    public void setDate(Date date){this.date = date;}

    public String getEmail(){
        return email;
    }
    public String getImg(){
        return img;
    }
    public Date getCheckInDate(){
        return checkInDate;
    }
    public Date getCheckOutDate(){
        return checkOutDate;
    }
    public LocationsModel getLocationsModel(){return location;}
    public Date getDate(){return date;}

    private String formatDate(Date date) {
        if (date == null) {
            return "";
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        return dateFormat.format(date);
    }
}
