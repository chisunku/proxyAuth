package com.example.checking.Model;

public class Employee {
    String name;
    String email;
    LocationsModel location;
    FaceModel face;
    String designation;
    String imageURL;
    String password;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    String userId;

    public void setName(String name){
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFace(FaceModel face) {
        this.face = face;
    }

    public void setLocationsModel(LocationsModel location) {
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

    public LocationsModel getLocationsModel() {
        return location;
    }

    public String getPassword() {
        return password;
    }
}


