package com.example.checking;

import static android.app.PendingIntent.FLAG_MUTABLE;
import static android.app.ProgressDialog.show;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.example.checking.Model.Location;
import com.example.checking.Service.APIService;
import com.example.checking.Service.FragmentChangeListener;
import com.example.checking.Service.RetrofitClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements FragmentChangeListener {
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng userLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    RecyclerView history;
    List<Attendance> dataHistory;
    AttendanceHistoryAdapter attendanceHistoryAdapter;
    CardView checkin;
    CardView checkOut;
    Attendance attendanceModel;

    Attendance fetchAttnedance;
    String TAG = "HomeFragment";
    Employee employee;
    private ProgressBar loadingProgressBar;

    ImageView profile_image;
    private PendingIntent pendingIntent;

    //Service
    Intent serviceIntent;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, parent, false);

        loadingProgressBar = view.findViewById(R.id.attendanceLoading);
        Log.d(TAG, "onCreateView: homefragment");
        //get employee
        Bundle arguments = getArguments();
        if (arguments != null) {
            employee = (Employee) arguments.getSerializable("Employee");
            if(employee == null)
                Log.d(TAG, "onCreateView: yes emp is null");
            Log.d(TAG, "onCreateView: emp name in fragment : "+employee.getImageURL());

            //saving email in shared preference for the service to access
            SharedPreferences.Editor editor = getActivity().getSharedPreferences("proxyAuth", Context.MODE_PRIVATE).edit();
            editor.putString("email", employee.getEmail());
            editor.apply();
        }
        else{
            Log.d(TAG, "onCreateView: arguments is null");
            getActivity().getFragmentManager().popBackStack();
        }

        //Service
        serviceIntent = new Intent(getContext(), LocationService.class);

        LocationService locationService = new LocationService();
        locationService.setFragmentChangeListener(this); // Assuming HomeFragment implements FragmentChangeListener


        //fetch attendance
        APIService apiService = RetrofitClient.getClient().create(APIService.class);
        Call<Attendance> call = apiService.getLatestRecord(employee.getEmail());
        Log.d(TAG, "onCreateView: call fetch latest attendance: ");
        call.enqueue(new Callback<Attendance>() {
            @Override
            public void onResponse(Call<Attendance> call, Response<Attendance> response) {
                System.out.println("response of fetch latest : "+response);
                if (response.isSuccessful()) {
                    fetchAttnedance = response.body();
                    Log.d(TAG, "onResponse: latest attendance : "+fetchAttnedance.getEmail()+" "+fetchAttnedance.getLocationsModel().getName());
                    if(fetchAttnedance!=null){
                        checkin.setEnabled(false);
                        TextView time = view.findViewById(R.id.checkInDate);
                        Instant instant = fetchAttnedance.getCheckInDate().toInstant();
                        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
                        time.setText(localDateTime.toLocalDate()+" @ "+localDateTime.getHour()+":"+String.format("%02d", localDateTime.getMinute()));
                        TextView blockName = view.findViewById(R.id.checkInBoxName);
                        blockName.setText("Checked In @ "+fetchAttnedance.getLocationsModel().getName());
                        getContext().startService(serviceIntent);

                        if(fetchAttnedance.getCheckOutDate()!=null){
                            checkOut.setEnabled(false);
                            time = view.findViewById(R.id.checkOutDate);
                            instant = fetchAttnedance.getCheckOutDate().toInstant();
                            localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
                            time.setText(localDateTime.toLocalDate()+" @ "+localDateTime.getHour()+":"+String.format("%02d", localDateTime.getMinute()));
                            blockName = view.findViewById(R.id.CheckOutBoxName);
                            blockName.setText("Checked out");
                        }
                        else{
                            checkOut.setEnabled(true);
                        }
                    }
                    else{
                        checkOut.setEnabled(false);
                    }
                } else {
                    System.out.println("API has no response");
                }
            }

            @Override
            public void onFailure(Call<Attendance> call, Throwable t) {
                // Handle network errors
                System.out.println("error in fetch latest attendance: " + call.toString()+" :: "+t.fillInStackTrace()+" "+t.getStackTrace()+" "+t.getMessage());
            }
        });

//        Drawable d = LoadImageFromWebOperations(employee.getImageURL());
//        Log.d(TAG, "onCreateView: drawable " + d.toString());
//        profile_image.setImageDrawable(d);

        //image from url
        profile_image = view.findViewById(R.id.profile_image);
        Picasso.get()
                .load(employee.getImageURL())
                .into(profile_image);
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
                                Log.d(TAG, "onCreateView: in after the face rec thing");
                                checkLogIn(getView());
                            }
                        });
        checkin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: employee name in homefragment "+employee.getEmail());
                Intent intent = new Intent(getActivity(), FaceRecognition.class);
                intent.putExtra("Employee", employee);
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

    public static Drawable LoadImageFromWebOperations(String url) {
        System.out.println("image url in convert to drawable : "+url);
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            if(d == null)
                Log.d("TAG", "LoadImageFromWebOperations: drawable is null");
            else{
                Log.d("TAG", "LoadImageFromWebOperations: drawable : "+d.toString());
            }
            return d;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void attendanceHistory(){
        APIService apiService = RetrofitClient.getClient().create(APIService.class);
        Call<List<Attendance>> call = apiService.getUserAttendance(employee.getEmail());
        System.out.println("call : ");
        call.enqueue(new Callback<List<Attendance>>() {
            @Override
            public void onResponse(Call<List<Attendance>> call, Response<List<Attendance>> response) {
                loadingProgressBar.setVisibility(View.GONE);
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

    @Override
    public void checkoutFronService(){
        checkout(view);
    }

    public void checkout(View view){
        getContext().stopService(serviceIntent);
        Log.d(TAG, "checkout: stop service called");
        APIService apiService = RetrofitClient.getClient().create(APIService.class);
        // Get the current date and time as LocalDateTime
        LocalDateTime currentDateTime = LocalDateTime.now();

        // Convert LocalDateTime to Instant
        Instant instant = currentDateTime.atZone(ZoneId.systemDefault()).toInstant();

        // Convert Instant to Date
        Date date = Date.from(instant);

        fetchAttnedance.setCheckOutDate(date);
        Call<Attendance> saveCall = apiService.checkout(fetchAttnedance);
        saveCall.enqueue(new Callback<Attendance>(){
            @Override
            public void onResponse(Call<Attendance> call, Response<Attendance> response) {
                Attendance att = response.body();
                System.out.println("response : "+response);
                TextView time = view.findViewById(R.id.checkOutDate);
                time.setText(currentDateTime.toLocalDate()+" @ "+currentDateTime.getHour()+":"+currentDateTime.getMinute());
                TextView boxName = view.findViewById(R.id.CheckOutBoxName);
                boxName.setText("Checked Out");
                dataHistory.get(0).setCheckOutDate(fetchAttnedance.getCheckOutDate());
                attendanceHistoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<Attendance> call, Throwable t) {
                System.out.println("failed to save checking: "+t.getStackTrace());
            }
        });
    }

    public void checkLogIn(View view){
        getContext().startService(serviceIntent);

        Log.d(TAG, "checkLogIn: in checkin");
        APIService apiService = RetrofitClient.getClient().create(APIService.class);

        Call<Location> call = apiService.checkLocation(userLocation.latitude, userLocation.longitude, employee.getEmail());
        call.enqueue(new Callback<Location>() {
            @Override
            public void onResponse(Call<Location> call, Response<Location> response) {
                if (response.isSuccessful()) {
                    Location res = response.body();
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
//                    attendanceModel.setDate(date);
                    fetchAttnedance = attendanceModel;
                    Log.d(TAG, "onResponse: attendance location : "+attendanceModel.getLocationsModel().getName());
                    Call<Attendance> saveCall = apiService.checkInUser(attendanceModel);
                    saveCall.enqueue(new Callback<Attendance>(){
                        @Override
                        public void onResponse(Call<Attendance> call, Response<Attendance> response) {
                            System.out.println("response checkin user : "+response+" msg : "+response.message()+" "+response.body());
                            Log.d(TAG, "onResponse: dataset size: "+dataHistory.size());
                            dataHistory.add(0, fetchAttnedance);
                            Log.d(TAG, "onResponse: dataset size after : "+dataHistory.size());
                            attendanceHistoryAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Call<Attendance> call, Throwable t) {
                            System.out.println("failed to save checking: "+t.getStackTrace());
                        }
                    });

                } else {
                    // Handle unsuccessful response
                    System.out.println("something went wrong in finding location : "+response);
                }
            }

            @Override
            public void onFailure(Call<Location> call, Throwable t) {
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
            Log.d(TAG, "requestLastLocation: request not granted!!!");
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
