package com.example.checking.Service;


import com.example.checking.Model.Attendance;
import com.example.checking.Model.Employee;
import com.example.checking.Model.LocationsModel;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface APIService {

    //get Attendance
    @GET("/getUserAttendance")
    Call<List<Attendance>> getAttendanceData();

    //get all locations
    @GET("/getAllLocations")
    Call<List<LocationsModel>> getAllLocations();

    //add location
    @POST("/addLocation")
    Call<String> addLocation(@Body LocationsModel locationsModel);

    @POST("/checkInUser")
    Call<Attendance> checkInUser(@Body Attendance attendanceModel);

    @POST("/checkout")
    Call<Attendance> checkout(@Body Attendance attendanceModel);

    //get attendance history
    @GET("/getUserAttendance")
    Call<List<Attendance>> getUserAttendance(@Query("email") String email);

    @GET("/isUserInsideAnyOffice")
    Call<LocationsModel> checkLocation(@Query("latitude") double latitude, @Query("longitude") double longitude);

    @POST("/registerEmp")
    Call<Employee> registerEmp(@Body Employee employee);

    @GET("/getEmployeeByEmail")
    Call<Employee> getEmployeeByEmail(@Query("email") String email);

    //Auth employee
    @GET("/emailAuth")
    Call<Employee> emailAuth(@Query("email") String email, @Query("password") String password, @Query("userId") String userId);

    @GET("/userIdAuth")
    Call<Employee> userIdAuth (@Query("userId") String userId);

//    getLatestRecord
    @GET("/getLatestRecord")
    Call<Attendance> getLatestRecord (@Query("email") String email);
}

