package com.example.checking;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.PolyUtil;

import java.util.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Attendance_adapter extends ArrayAdapter<attendance_card_model> {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private FirebaseFirestore db;
    ArrayList<LocationsModel> dataList;
    private LatLng userLocation;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    Activity activity;
    TextView time;


    public Attendance_adapter(@NonNull Context context, ArrayList<attendance_card_model> courseModelArrayList, LatLng userLocation, Activity activity) {
        super(context, 0, courseModelArrayList);
        this.userLocation = userLocation;
        this.db = FirebaseFirestore.getInstance();
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        this.activity = activity;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        System.out.println("userLocation : " + userLocation);
        requestLastLocation();
        View listitemView = convertView;
        if (listitemView == null) {
            // Layout Inflater inflates each item to be displayed in GridView.
            listitemView = LayoutInflater.from(getContext()).inflate(R.layout.activity_card_attendance_box, parent, false);
        }

        attendance_card_model courseModel = getItem(position);
        CardView cv = listitemView.findViewById(R.id.card);
//        TextView status = listitemView.findViewById(R.id.status);
        TextView boxname = listitemView.findViewById(R.id.boxName);
        time = listitemView.findViewById(R.id.time);
        ImageView courseIV = listitemView.findViewById(R.id.icon);

//        status.setText(courseModel.getStatus());
        boxname.setText(courseModel.getBoxName());
        time.setText(String.valueOf(courseModel.getTime()));
        courseIV.setImageResource(courseModel.getImgId());

        System.out.println("boxname.getText().toString() : "+boxname.getText().toString());

        cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("In the on click thing!!!");
                if("Check In".equals(boxname.getText().toString())) {
                    markAttendance();
                }
                else if ("Check Out".equals(boxname.getText().toString())) {
                    System.out.println("Check Out!!");
                }
            }
        });
        return listitemView;
    }

    public void markAttendance(){
        new Attendance_adapter.FetchDataAsyncTask().execute();

    }

    public class FetchDataAsyncTask extends AsyncTask<Void, Void, Void> {

        public FetchDataAsyncTask() {

        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            System.out.println("location : "+userLocation+" "+dataList.size());
            // Check if the user is inside the polygon
            int att_flag = 0;
            if(dataList.size()>0 && userLocation!=null) {
                //check for all the locations
                for(LocationsModel model : dataList) {
                    boolean isInside = PolyUtil.containsLocation(userLocation, convertPointsToLatLngList(model.getPolygon()), true);
                    System.out.println("Is inside polygon: " + isInside);
                    if(isInside) {
                        Toast.makeText(getContext(), "Attendance Marked: "+model.getName()+" office." , Toast.LENGTH_SHORT).show();
                        att_flag = 1;
                        //set the checkin time if the user is inside boundary
                        CollectionReference attendanceRef = db.collection("attendance");
//                        if(attendanceRef.document("test@gmail.com")!=null){
//                            DocumentReference documentReference = attendanceRef.document("test@gmail.com");
//                            System.out.println("Already marked: "+documentReference.toString());
//                        }
//                        else{
                            Timestamp check = new Timestamp(System.currentTimeMillis());
                            Date date = new Date(check.getTime());
                            Attendance_model attendance_model = new Attendance_model("test@gmail.com", check, "Check In", date, R.drawable.checkin);
                            time.setText(check.toString());
                            attendanceRef.document("test@gmail.com")
                                    .set(attendance_model)
                                    .addOnSuccessListener(aVoid -> {
                                        // Document added successfully
                                        System.out.println("Document added with custom ID: " + "test@gmail.com");
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle errors
                                        System.out.println("Error adding document: " + e.getMessage());
                                    });
//                        }
                        return;
                    }
                }
                if(att_flag == 0)
                    Toast.makeText(getContext(), "Outside Geofencing location. If theres an issue call the HR.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dataList = new ArrayList<LocationsModel>();
            CollectionReference collectionRef = db.collection("cities");
            Task<QuerySnapshot> task = collectionRef.get();
            task.addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    QuerySnapshot querySnapshot = task1.getResult();
                    if (querySnapshot != null) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            LocationsModel model = document.toObject(LocationsModel.class);
                            System.out.println("data : " + document.getData());
                            System.out.println("after data: " + model.getName());
                            dataList.add(model);
                        }
                    }
                } else {
                    // Handle errors
                    Exception exception = task1.getException();
                    if (exception != null) {
                        // Handle the exception
                    }
                }
            });

            // Wait for the Firestore operation to complete
            try {
                Tasks.await(task);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    // Helper method to convert List<Point> to List<LatLng>
    private List<LatLng> convertPointsToLatLngList(List<LocationsModel.Point> points) {
        List<LatLng> latLngList = new ArrayList<>();
        for (LocationsModel.Point point : points) {
            latLngList.add(new LatLng(point.getLatitude(), point.getLongitude()));
        }
        return latLngList;
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
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

}
