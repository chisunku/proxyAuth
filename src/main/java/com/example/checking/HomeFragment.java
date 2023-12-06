package com.example.checking;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.PolyUtil;

import java.io.FileReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class HomeFragment extends Fragment {
    private FusedLocationProviderClient fusedLocationProviderClient;
    private FirebaseFirestore db;
    ArrayList<LocationsModel> dataList;
    private LatLng userLocation;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    RecyclerView coursesGV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, parent, false);
        db = FirebaseFirestore.getInstance();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        requestLastLocation();
        System.out.println("userLocation : " + userLocation);
//        Button officeLocations = view.findViewById(R.id.officeLocations);
//        officeLocations.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ((MainActivity) requireActivity()).updateBottomNavigation(R.id.location);
//                LocationListView fragment = new LocationListView();
//                FragmentManager fragmentManager = getFragmentManager();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.content, fragment, "");
//                fragmentTransaction.addToBackStack("location");
//                fragmentTransaction.commit();
//            }
//        });

//        Button attendance = view.findViewById(R.id.markAttendance);
//        attendance.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                markAttendance();
//            }
//        });

        coursesGV = view.findViewById(R.id.idGVcourses);
        ArrayList<Attendance_model> courseModelArrayList = new ArrayList<Attendance_model>();

        courseModelArrayList.add(new Attendance_model("", null,"Check In", null, R.drawable.checkin));
        courseModelArrayList.add(new Attendance_model("", null,"Check Out", null, R.drawable.checkout));
        FragmentManager fragmentManager = getFragmentManager();
//        Attendance_adapter adapter = new Attendance_adapter(getContext(), courseModelArrayList, userLocation, getActivity());
        //Context context, ArrayList<Attendance_model> locationModelArrayList, FragmentManager fragmentManager, LatLng userLocation
        attendace_recycler_adapter adapter1 = new attendace_recycler_adapter(getContext(), courseModelArrayList, fragmentManager, userLocation);
//        coursesGV.setAdapter(adapter1);
        int spanCount = 2;
//        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
//
//        // Set the orientation of the grid (vertical or horizontal)
//        layoutManager.setOrientation(GridLayoutManager.VERTICAL);
        coursesGV.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        // Attach the layoutManager to the recyclerView
        coursesGV.setAdapter(adapter1);

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
                // Permission granted, proceed with location-related tasks
                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
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

//    public void markAttendance(){
//        new FetchDataAsyncTask().execute();
//
//    }

//    public class FetchDataAsyncTask extends AsyncTask<Void, Void, Void> {
//
//        public FetchDataAsyncTask() {
//
//        }
//
//        @Override
//        protected void onPostExecute(Void unused) {
//            super.onPostExecute(unused);
//            System.out.println("location : "+userLocation+" "+dataList.size());
//            // Check if the user is inside the polygon
//            int att_flag = 0;
//            if(dataList.size()>0 && userLocation!=null) {
//                //check for all the locations
//                for(LocationsModel model : dataList) {
//                    boolean isInside = PolyUtil.containsLocation(userLocation, convertPointsToLatLngList(model.getPolygon()), true);
//                    System.out.println("Is inside polygon: " + isInside);
//                    if(isInside) {
//                        Toast.makeText(getContext(), "Attendance Marked: "+model.getName()+" office." , Toast.LENGTH_SHORT).show();
//                        att_flag = 1;
//                        break;
//                    }
//                }
//                if(att_flag == 0)
//                    Toast.makeText(getContext(), "Outside Geofencing location. If theres an issue call the HR.", Toast.LENGTH_SHORT).show();
//            }
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            dataList = new ArrayList<LocationsModel>();
//            CollectionReference collectionRef = db.collection("cities");
//            Task<QuerySnapshot> task = collectionRef.get();
//            task.addOnCompleteListener(task1 -> {
//                if (task1.isSuccessful()) {
//                    QuerySnapshot querySnapshot = task1.getResult();
//                    if (querySnapshot != null) {
//                        for (QueryDocumentSnapshot document : querySnapshot) {
//                            LocationsModel model = document.toObject(LocationsModel.class);
//                            System.out.println("data : " + document.getData());
//                            System.out.println("after data: " + model.getName());
//                            dataList.add(model);
//                        }
//                    }
//                } else {
//                    // Handle errors
//                    Exception exception = task1.getException();
//                    if (exception != null) {
//                        // Handle the exception
//                    }
//                }
//            });
//
//            // Wait for the Firestore operation to complete
//            try {
//                Tasks.await(task);
//            } catch (ExecutionException | InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//    }

    // Helper method to convert List<Point> to List<LatLng>
//    private List<LatLng> convertPointsToLatLngList(List<LocationsModel.Point> points) {
//        List<LatLng> latLngList = new ArrayList<>();
//        for (LocationsModel.Point point : points) {
//            latLngList.add(new LatLng(point.getLatitude(), point.getLongitude()));
//        }
//        return latLngList;
//    }

}
