package com.example.checking;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.checking.Model.AttendanceModel;
import com.example.checking.Model.LocationsModel;
import com.example.checking.Service.APIService;
import com.example.checking.Service.RetrofitClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private FusedLocationProviderClient fusedLocationProviderClient;
    private FirebaseFirestore db;
    private LatLng userLocation;
    ArrayList<AttendanceModel> courseModelArrayList;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    RecyclerView coursesGV;
    RecyclerView history;
    String checkintime = "not Checked in";
    String checkoutTime = "not Checked out";

    String checkinDate = "";
    String checkoutDate = "";
    List<AttendanceModel> dataHistory;

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
                        AttendanceModel data = document.toObject(AttendanceModel.class);
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
            String CheckinBox = "Check In";
            String CheckoutBox = "Check Out";
            if(!checkintime.equals("not Checked in")){
                CheckinBox = "Checked In";
            }
            if(!checkoutTime.equals("not Checked out")){
                CheckoutBox = "Checked Out";
            }
            courseModelArrayList.add(new AttendanceModel("", checkintime ,CheckinBox, checkinDate, R.drawable.checkin));
            courseModelArrayList.add(new AttendanceModel("", checkoutTime,CheckoutBox, checkoutDate, R.drawable.checkout));
            FragmentManager fragmentManager = getFragmentManager();
            attendace_recycler_adapter adapter1 = new attendace_recycler_adapter(getContext(), courseModelArrayList, fragmentManager, userLocation);
            int spanCount = 2;
            coursesGV.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
            coursesGV.setAdapter(adapter1);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            courseModelArrayList = new ArrayList<>();
//            Timestamp today = new Timestamp(System.currentTimeMillis());
//            SimpleDateFormat sdf1 = new SimpleDateFormat("d MMM YY");
//            String formattedDate = sdf1.format(today);
            Date checkinTime = new Date();
            System.out.println("formattedDate : " + checkinTime);
            CollectionReference documentRef = db.collection("attendance");
            try{
                //call updateCheckIn API
                APIService apiService = RetrofitClient.getClient().create(APIService.class);

                Call<List<LocationsModel>> call = apiService.getAllLocations();

                call.enqueue(new Callback<List<LocationsModel>>() {
                    @Override
                    public void onResponse(Call<List<LocationsModel>> call, Response<List<LocationsModel>> response) {
                        if (response.isSuccessful()) {
                            List<LocationsModel> attendanceList = response.body();
                            System.out.println("location list : "+attendanceList);
                            // Handle the list of AttendanceModel objects
                        } else {
                            // Handle unsuccessful response
                        }
                    }

                    @Override
                    public void onFailure(Call<List<LocationsModel>> call, Throwable t) {
                        // Handle network errors
                    }
                });
            }catch(Exception e){
                e.printStackTrace();
            }

            return null;
        }

    }


}
