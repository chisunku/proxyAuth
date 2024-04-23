package com.example.checking.Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Employee implements Serializable {
//    String id;
    String name;
    String email;
    Location location;
    FaceModel face;
    String designation;
    String imageURL;
    String password;
    String userId;
    String address;
    double latitude;
    double longitude;
    String contactNo;

//    public void getID(String id){
//        this.id = id;
//    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getContactNo() {
        return contactNo;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFace(FaceModel face) {
        this.face = face;
    }

    public void setLocationsModel(Location location) {
        this.location = location;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDesignation() {
        return designation;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public FaceModel getFace() {
        return face;
    }

    public Location getLocationsModel() {
        return location;
    }

    public String getPassword() {
        return password;
    }
}


