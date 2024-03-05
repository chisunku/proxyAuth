package com.example.checking;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.checking.Model.Attendance;
import com.example.checking.Model.Employee;
import com.example.checking.Model.LocationsModel;
import com.example.checking.Service.APIService;
import com.example.checking.Service.RetrofitClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng userLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    RecyclerView history;
    List<Attendance> dataHistory;
    AttendanceHistoryAdapter attendanceHistoryAdapter;
    CardView checkin;
    CardView checkOut;
    Attendance attendanceModel;
    String TAG = "HomeFragment";
    Employee employee;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, parent, false);

        //get employee
        Bundle arguments = getArguments();
        if (arguments != null) {
            employee = (Employee) arguments.getSerializable("Employee");
            Log.d(TAG, "onCreateView: emp name in fragment : "+employee.getDesignation());
        }
        else{
            Log.d(TAG, "onCreateView: arguments is null");
        }

        TextView name = view.findViewById(R.id.name);
        name.setText(employee.getName());

        TextView designation = view.findViewById(R.id.designation);
        designation.setText(employee.getDesignation());

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        requestLastLocation();
        Log.d(TAG, "onCreateView: userLocation : " + userLocation);
        fetchAttendanceHistory();
        attendanceHistory();
        history = view.findViewById(R.id.attendanceHistory);
        checkin = view.findViewById(R.id.checkinBoxCardView);

        final ActivityResultLauncher<Intent> faceRecognitionLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                // Handle the result, for example, call checkLogIn
                                checkLogIn(getView());
                            }
                        });

        checkin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FaceRecognition.class);
                faceRecognitionLauncher.launch(intent);
            }
        });


        checkOut = view.findViewById(R.id.checkoutBoxCardView);
        checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkout(view);
            }
        });
        return view;
    }

    private void attendanceHistory(){
        APIService apiService = RetrofitClient.getClient().create(APIService.class);
        Call<List<Attendance>> call = apiService.getUserAttendance(employee.getEmail());
        System.out.println("call : ");
        call.enqueue(new Callback<List<Attendance>>() {
            @Override
            public void onResponse(Call<List<Attendance>> call, Response<List<Attendance>> response) {
                System.out.println("response: "+response);
                if (response.isSuccessful()) {
                    dataHistory = response.body();
                    System.out.println("location list : "+dataHistory);
                    FragmentManager fragmentManager = getFragmentManager();
                    attendanceHistoryAdapter = new AttendanceHistoryAdapter(getContext(), dataHistory);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext(),
                            LinearLayoutManager.VERTICAL, false);

                    // in below two lines we are setting layoutmanager and adapter to our recycler view.
                    history.setLayoutManager(linearLayoutManager);
                    history.setAdapter(attendanceHistoryAdapter);
                    // Handle the list of AttendanceModel objects
                } else {
                    System.out.println("API has no response");
                    // Handle unsuccessful response
                }
            }

            @Override
            public void onFailure(Call<List<Attendance>> call, Throwable t) {
                // Handle network errors
                System.out.println("error: " + t.fillInStackTrace());
            }
        });
    }

//    private void check(View view){
    //get latest record and check if its today
//        APIService apiService = RetrofitClient.getClient().create(APIService.class);
//        Call<Attendance> saveCall = apiService.checkInUser(attendanceModel);
//    }

    private void checkout(View view){
        APIService apiService = RetrofitClient.getClient().create(APIService.class);
        // Get the current date and time as LocalDateTime
        LocalDateTime currentDateTime = LocalDateTime.now();

        // Convert LocalDateTime to Instant
        Instant instant = currentDateTime.atZone(ZoneId.systemDefault()).toInstant();

        // Convert Instant to Date
        Date date = Date.from(instant);

        attendanceModel.setCheckOutDate(date);
        Call<Attendance> saveCall = apiService.checkInUser(attendanceModel);
        saveCall.enqueue(new Callback<Attendance>(){
            @Override
            public void onResponse(Call<Attendance> call, Response<Attendance> response) {
                Attendance att = response.body();
                System.out.println("response : "+response);
                TextView time = view.findViewById(R.id.checkOutDate);
                time.setText(currentDateTime.toLocalDate()+" @ "+currentDateTime.getHour()+":"+currentDateTime.getMinute());
                TextView boxName = view.findViewById(R.id.CheckOutBoxName);
                boxName.setText("Checked Out");
            }

            @Override
            public void onFailure(Call<Attendance> call, Throwable t) {
                System.out.println("failed to save checking: "+t.getStackTrace());
            }
        });
    }

    public void checkLogIn(View view){
        Log.d(TAG, "checkLogIn: in checkin");
        APIService apiService = RetrofitClient.getClient().create(APIService.class);

        Call<LocationsModel> call = apiService.checkLocation(userLocation.latitude, userLocation.longitude);
        call.enqueue(new Callback<LocationsModel>() {
            @Override
            public void onResponse(Call<LocationsModel> call, Response<LocationsModel> response) {
                if (response.isSuccessful()) {
                    LocationsModel res = response.body();
                    System.out.println("Attendance marked ? "+ res.getName());
                    // Handle the list of AttendanceModel objects
                    Toast.makeText(getContext(), "User Checked In @ "+res.getName(), Toast.LENGTH_SHORT).show();
                    TextView time = view.findViewById(R.id.checkInDate);

                    // Get the current date and time as LocalDateTime
                    LocalDateTime currentDateTime = LocalDateTime.now();

                    // Convert LocalDateTime to Instant
                    Instant instant = currentDateTime.atZone(ZoneId.systemDefault()).toInstant();

                    // Convert Instant to Date
                    Date date = Date.from(instant);

                    System.out.println("currentDateTime : " + " date : "+date);

                    time.setText(currentDateTime.toLocalDate()+" @ "+currentDateTime.getHour()+":"+currentDateTime.getMinute());

                    TextView blockName = view.findViewById(R.id.checkInBoxName);
                    blockName.setText("Checked In @ "+res.getName());

                    attendanceModel = new Attendance();
                    attendanceModel.setLocationsModel(res);
                    attendanceModel.setCheckInDate(date);
                    attendanceModel.setEmail(employee.getEmail());
                    attendanceModel.setDate(date);

                    Call<Attendance> saveCall = apiService.checkInUser(attendanceModel);
                    saveCall.enqueue(new Callback<Attendance>(){
                        @Override
                        public void onResponse(Call<Attendance> call, Response<Attendance> response) {
                            System.out.println("response : "+response);
                        }

                        @Override
                        public void onFailure(Call<Attendance> call, Throwable t) {
                            System.out.println("failed to save checking: "+t.getStackTrace());
                        }
                    });

                } else {
                    // Handle unsuccessful response
                    System.out.println("something went wrong in finding location");
                }
            }

            @Override
            public void onFailure(Call<LocationsModel> call, Throwable t) {
                Log.e(TAG, "checkin error"+t.getMessage()+" "+call.toString());
                // Handle network errors
                System.out.println("error: "+t.getStackTrace());
            }
        });
    }

    private void requestLastLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            System.out.println("in if");
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            System.out.println("in if --> if");
                            userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            System.out.println("location : " + userLocation);
//                            checkLogIn();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to get location", Toast.LENGTH_SHORT).show();
                    });
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                System.out.println("in if --> if");
                                userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                System.out.println("location : " + userLocation);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to get location", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Permission denied, handle accordingly
                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void fetchAttendanceHistory(){
        APIService apiService = RetrofitClient.getClient().create(APIService.class);
        Call<List<Attendance>> call = apiService.getUserAttendance(employee.getEmail());
        System.out.println("call : ");
        call.enqueue(new Callback<List<Attendance>>() {
            @Override
            public void onResponse(Call<List<Attendance>> call, Response<List<Attendance>> response) {
                System.out.println("response: "+response);
                if (response.isSuccessful()) {
                    dataHistory = response.body();
                    System.out.println("location list : "+dataHistory);
                } else {
                    System.out.println("API has no response");
                }
            }

            @Override
            public void onFailure(Call<List<Attendance>> call, Throwable t) {
                // Handle network errors
                System.out.println("error: " + t.fillInStackTrace());
            }
        });
    }
}
