package com.example.checking;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class HomeFragment extends Fragment {
    private FusedLocationProviderClient fusedLocationProviderClient;
    private FirebaseFirestore db;
    private LatLng userLocation;
    ArrayList<Attendance_model> courseModelArrayList;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    RecyclerView coursesGV;
    RecyclerView history;
    String checkintime = "not Checked in";
    String checkoutTime = "not Checked out";

    String checkinDate = "";
    String checkoutDate = "";
    List<Attendance_model> dataHistory;
    Timestamp today;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, parent, false);
        db = FirebaseFirestore.getInstance();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        requestLastLocation();
        System.out.println("userLocation : " + userLocation);
        new AttendanceHistory().execute();
        new FetchDataAsyncTask().execute();
        coursesGV = view.findViewById(R.id.idGVcourses);
        history = view.findViewById(R.id.attendanceHistory);

        return view;
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
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to get location", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Permission is not yet granted
            // Request the permission
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

    public class AttendanceHistory extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            // Perform your Firestore query here
            CollectionReference dataCollection = db.collection("attendance");

            Task<QuerySnapshot> task = dataCollection.get();

            try {
                // Block on the task to retrieve the result synchronously
                Tasks.await(task);

                if (task.isSuccessful()) {
                    dataHistory = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Attendance_model data = document.toObject(Attendance_model.class);
                        System.out.println("data : "+data.getDate()+" "+data.getEmail());
                        dataHistory.add(data);
                    }
                    System.out.println("data size : "+ dataHistory.size());
                } else {
                    // Handle errors
                    Exception e = task.getException();
                    if (e != null) {
                        e.printStackTrace();
                    }
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if (dataHistory != null) {
                // Update UI with the fetched data
                System.out.println("in the if "+dataHistory.size());
                AttendanceHistoryAdapter adapter = new AttendanceHistoryAdapter(getContext(), dataHistory);
                history.setLayoutManager(new LinearLayoutManager(getContext()));
                history.setAdapter(adapter);
            } else {
                // Handle the case where data retrieval failed
                System.out.println("data : "+dataHistory);
                Toast.makeText(getContext(), "Failed to retrieve attendance history", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class FetchDataAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            courseModelArrayList.add(new Attendance_model("", checkintime,"Check In", checkinDate, R.drawable.checkin));
            courseModelArrayList.add(new Attendance_model("", checkoutTime,"Check Out", checkoutDate, R.drawable.checkout));
            FragmentManager fragmentManager = getFragmentManager();
            attendace_recycler_adapter adapter1 = new attendace_recycler_adapter(getContext(), courseModelArrayList, fragmentManager, userLocation);
            int spanCount = 2;
            coursesGV.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
            coursesGV.setAdapter(adapter1);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            courseModelArrayList = new ArrayList<>();
            Timestamp today = new Timestamp(System.currentTimeMillis());
            SimpleDateFormat sdf1 = new SimpleDateFormat("d MMM YY");
            String formattedDate = sdf1.format(today);
            System.out.println("formattedDate : " + formattedDate);
            CollectionReference documentRef = db.collection("attendance");

            try {
                // Block on the task to retrieve the result synchronously
                Task<QuerySnapshot> task = documentRef
                        .whereEqualTo("email", "test@gmail.com") // Replace with the actual user's email
                        .whereEqualTo("date", formattedDate)
                        .whereEqualTo("timeRef", "Check In")
                        .get();

                Tasks.await(task);

                if (task.isSuccessful()) {
                    // Handle the task result and extract the attendance records for the specified date
                    List<Attendance_model> attendanceList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Attendance_model attendanceRecord = document.toObject(Attendance_model.class);
                        System.out.println("data in fetch : " + attendanceRecord.getEmail() + " " + attendanceRecord.getTime());
                        attendanceList.add(attendanceRecord);
                        checkintime = attendanceRecord.getTime();
                        checkinDate = attendanceRecord.getDate();
                    }
                    // Do something with the attendanceList for the specified date
                } else {
                    System.out.println("No data found!!!");
                    // Handle errors
                    Exception e = task.getException();
                    if (e != null) {
                        e.printStackTrace();
                    }
                }

                // Block on the task to retrieve the result synchronously -- checkout
                Task<QuerySnapshot> task1 = documentRef
                        .whereEqualTo("email", "test@gmail.com") // Replace with the actual user's email
                        .whereEqualTo("date", formattedDate)
                        .whereEqualTo("timeRef", "Check Out")
                        .get();

                Tasks.await(task1);

                if (task.isSuccessful()) {
                    // Handle the task result and extract the attendance records for the specified date
                    List<Attendance_model> attendanceList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task1.getResult()) {
                        Attendance_model attendanceRecord = document.toObject(Attendance_model.class);
                        System.out.println("data in fetch : " + attendanceRecord.getEmail() + " " + attendanceRecord.getTime());
                        attendanceList.add(attendanceRecord);
                        checkoutTime = attendanceRecord.getTime();
                        checkoutDate = attendanceRecord.getDate();
                    }
                } else {
                    System.out.println("No data found!!!");
                    // Handle errors
                    Exception e = task.getException();
                    if (e != null) {
                        e.printStackTrace();
                    }
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

    }


}