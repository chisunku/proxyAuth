package com.example.checking.Service;


import com.example.checking.Model.Attendance;
import com.example.checking.Model.Employee;
import com.example.checking.Model.Leaves;
import com.example.checking.Model.Location;

import java.util.Date;
import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIService {

    @GET("/")
    Call<String> test();

    //get all locations
    @GET("/getAllLocations")
    Call<List<Location>> getAllLocations();

    //add location
    @POST("/addLocation")
    Call<String> addLocation(@Body Location location);

    @POST("/checkInUser")
    Call<Attendance> checkInUser(@Body Attendance attendanceModel);

    @POST("/checkout")
    Call<Attendance> checkout(@Body Attendance attendanceModel);

    @POST("/checkoutLocation")
    Call<Employee> checkoutLocation(@Body Employee employee);

    //get attendance history
    @GET("/getUserAttendance")
    Call<List<Attendance>> getUserAttendance(@Query("email") String email);

    @GET("/isUserInsideAnyOffice")
    Call<Location> checkLocation(@Query("latitude") double latitude, @Query("longitude") double longitude, @Query("email") String email);

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

    //register employee
    @POST("/registerEmployee")
    Call<Employee> registerEmployee(@Body Employee employee);

    @Multipart
    @POST("/checking")
    Call<String> checking (@Part MultipartBody.Part file);

    @Multipart
    @POST("/putImageToBucket")
    Call<String> putImageToBucket (@Part MultipartBody.Part file, @Part("file_name") String file_name);

    @DELETE("/deleteLocation")
    Call<Void> deleteLocation (@Query("locationId") String locationId);

    @GET("/leave/{employeeId}/")
    Call<List<Leaves>> getLeave(@Path("employeeId") String employeeId);

    @GET("/getPastLeave")
    Call<List<Leaves>> getPastLeave(@Query("employeeEmail") String employeeEmail, @Query("endDate") Date endDate);

    //getUpcomingLeave
    @GET("/getUpcomingLeave")
    Call<List<Leaves>> getUpcomingLeave(@Query("employeeEmail") String employeeEmail, @Query("startDate") Date startDate);

    @GET("/getLeavesStatusCount")
    Call<List<Leaves>> getLeavesStatusCount(@Query("employeeEmail") String employeeEmail);

    @POST("/leave")
    Call<Leaves> addLeave(@Body Leaves leaves);

    @GET("/leave/{employeeEmail}/{status}")
    Call<List<Leaves>> getLeavesByFilter(@Path("employeeEmail") String employeeEmail, @Path("status") String status);

    @GET("/leave/notApproved")
    Call<List<Leaves>> getNotApprovedLeaves(@Query("startDate") Date startDate);

    @POST("/leave/approve/{id}")
    Call<String> approveLeave(@Path("id") String id, @Query("approvedBy") String approvedBy);

    @POST("/leave/reject/{id}")
    Call<String> rejectLeave(@Path("id") String id, @Query("rejectReason") String rejectReason, @Query("approvedBy") String approvedBy);

    @GET("/adminEmail")
    Call<Employee> adminEmail(@Query("email") String email, @Query("password") String password);

    @GET("/leave/getAllAfterStartDate")
    Call<List<Leaves>> getAllAfterStartDate(@Query("startDate") Date startDate);

    @GET("/getAllEmployees")
    Call<List<Employee>> getAllEmployees();

    @Multipart
    @POST("/addEmployees")
    Call<Boolean> addEmployees(@Part MultipartBody.Part file, @Part("name") String name);

    @POST("updateUserLocation")
    Call<String> updateUserLocation(@Query("email") String email, @Query("latitude") double latitude, @Query("longitude") double longitude);

}

