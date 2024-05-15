package com.example.checking.Model;

import java.io.Serializable;
import java.util.Date;

public class Attendance implements Serializable {
    String email;
    Date checkInDate;
    Date checkOutDate;
    private Location location;

    public void setEmail(String email){
        this.email = email;
    }
    public void setCheckInDate(Date checkInDate){
        this.checkInDate = checkInDate;
    }
    public void setCheckOutDate(Date checkOutDate){
        this.checkOutDate = checkOutDate;
    }
    public void setLocationsModel(Location location){this.location = location;}
    public String getEmail(){
        return email;
    }
    public Date getCheckInDate(){
        return checkInDate;
    }
    public Date getCheckOutDate(){
        return checkOutDate;
    }
    public Location getLocationsModel(){return location;}

}
