package com.example.checking.Service;


import com.example.checking.Model.AttendanceModel;
import com.example.checking.Model.LocationsModel;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface APIService {

    //get Attendance
    @GET("/getUserAttendance")
    Call<List<AttendanceModel>> getAttendanceData();

    //get all locations
    @GET("/getAllLocations")
    Call<List<LocationsModel>> getAllLocations();
}

