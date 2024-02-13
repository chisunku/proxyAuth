package com.example.checking;

        import android.content.Context;
        import android.content.pm.PackageManager;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.TextView;
        import android.widget.Toast;

        import androidx.annotation.NonNull;
        import androidx.cardview.widget.CardView;
        import androidx.core.content.ContextCompat;
        import androidx.fragment.app.FragmentManager;
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
        import com.google.maps.android.PolyUtil;

        import java.io.Serializable;
        import java.sql.Timestamp;
        import java.text.SimpleDateFormat;
        import java.util.ArrayList;
        import java.util.Date;
        import java.util.List;
        import java.util.concurrent.ExecutionException;

public class attendace_recycler_adapter extends RecyclerView.Adapter<attendace_recycler_adapter.handler> {

    private final Context context;
    private final ArrayList<Attendance_model> locationModelArrayList;
    FragmentManager fragmentManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private FirebaseFirestore db;
    ArrayList<LocationsModel> dataList;
    private LatLng userLocation;
    LocationsModel currentLocation;
    Attendance_model attendance_model;
    boolean checkedin = false;

    // Constructor
    public attendace_recycler_adapter(Context context, ArrayList<Attendance_model> locationModelArrayList, FragmentManager fragmentManager, LatLng userLocation) {
        this.context = context;
        this.locationModelArrayList = locationModelArrayList;
        this.fragmentManager = fragmentManager;
        this.userLocation = userLocation;
        this.db = FirebaseFirestore.getInstance();
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        System.out.println("in adapter constructor");
    }

    @NonNull
    @Override
    public attendace_recycler_adapter.handler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // to inflate the layout for each item of recycler view.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_card_attendance_box, parent, false);
        System.out.println("in adapter binder on create view");
        return new handler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull attendace_recycler_adapter.handler holder, int position) {
        // to set data to textview and imageview of each card layout
        System.out.println("in adapter binder holder");
        Attendance_model model = locationModelArrayList.get(position);
        holder.boxName.setText(model.getTimeRef());
        holder.time.setText("" + model.getTime());
        holder.date.setText(""+model.getDate());
        holder.img.setImageResource(model.getImgId());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                callback.onItemClicked();
                requestLastLocation();
                if("Check In".equals(holder.boxName.getText().toString())) {
                    markAttendance(holder);
                }
                else if ("Check Out".equals(holder.boxName.getText().toString())) {
                    System.out.println("Check Out!!");
                    martCheckOut(holder);
                }
                else if("Checked In".equals(holder.boxName.getText().toString())){
                    checkedin = true;
                    new attendace_recycler_adapter.fetchLocation().execute();
                    if(currentLocation!=null){
                        ShowAttendanceLocation yourFragment = new ShowAttendanceLocation();
                        Bundle args = new Bundle();
                        args.putSerializable("loc", (Serializable) currentLocation);
                        args.putSerializable("attendance", (Serializable) model);
                        yourFragment.setArguments(args);
                        fragmentManager.beginTransaction()
                                .replace(R.id.content, yourFragment, "location")
                                .addToBackStack("location")
                                .commit();
                    }
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        // this method is used for showing number of card items in recycler view
        return locationModelArrayList.size();
    }

    // View holder class for initializing of your views such as TextView and Imageview
    public static class handler extends RecyclerView.ViewHolder {
        //        private final ImageView courseIV;
        private final TextView boxName;
        private final TextView time;
        private final TextView date;
        private CardView cardView;
        private ImageView img;

        public handler(View itemView) {
            super(itemView);
            boxName = itemView.findViewById(R.id.boxName);
            time = itemView.findViewById(R.id.time);
            cardView = itemView.findViewById(R.id.card);
            img = itemView.findViewById(R.id.icon);
            date = itemView.findViewById(R.id.Date);
        }
    }

    public void markAttendance(attendace_recycler_adapter.handler holder){
        new attendace_recycler_adapter.FetchDataAsyncTask(holder).execute();

    }

    public void martCheckOut(attendace_recycler_adapter.handler holder){
        new attendace_recycler_adapter.checkout(holder).execute();

    }

    public class fetchLocation extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            CollectionReference documentRef = db.collection("attendance");
            try {
                Timestamp today = new Timestamp(System.currentTimeMillis());
                SimpleDateFormat sdf1 = new SimpleDateFormat("d MMM YY");
                String formattedDate = sdf1.format(today);
                // Block on the task to retrieve the result synchronously
                Task<QuerySnapshot> task = documentRef
                        .whereEqualTo("email", "test@gmail.com") // Replace with the actual user's email
                        .whereEqualTo("date", formattedDate)
                        .whereEqualTo("timeRef", "Checked In")
                        .get();

                Tasks.await(task);

                if (task.isSuccessful()) {
                    // Handle the task result and extract the attendance records for the specified date
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Attendance_model attendanceRecord = document.toObject(Attendance_model.class);
                        currentLocation = attendanceRecord.getLocationsModel();
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
                return null;
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public class checkout extends AsyncTask<Void, Void, Void> {

        private attendace_recycler_adapter.handler viewHolder;

        // Constructor to receive the ViewHolder
        public checkout(attendace_recycler_adapter.handler viewHolder) {
            this.viewHolder = viewHolder;
        }


        @Override
        protected Void doInBackground(Void... voids) {
            CollectionReference attendance = db.collection("attendance");
            Timestamp check = new Timestamp(System.currentTimeMillis());
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            String formattedTime = sdf.format(check);
            System.out.println("time : "+formattedTime);
            SimpleDateFormat sdf1 = new SimpleDateFormat("d MMM YY");
            String formattedDate = sdf1.format(check);
            Attendance_model attendance_model = new Attendance_model("test@gmail.com", formattedTime, "Checked Out", formattedDate, R.drawable.checkout);

            attendance
                    .add(attendance_model)
                    .addOnSuccessListener(documentReference -> {
                        viewHolder.time.setText(formattedTime);
                    })
                    .addOnFailureListener(e -> {
                    });
            return null;
        }
    }

    public class FetchDataAsyncTask extends AsyncTask<Void, Void, Void> {

        private attendace_recycler_adapter.handler viewHolder;

        // Constructor to receive the ViewHolder
        public FetchDataAsyncTask(attendace_recycler_adapter.handler viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
                // Check if the user is inside the polygon
                int att_flag = 0;
                if (currentLocation!=null) {
                            System.out.println("the location : "+ currentLocation.getName());
                            if(!checkedin) {
                            Toast.makeText(context, "Attendance Marked: " + currentLocation.getName() + " office.", Toast.LENGTH_SHORT).show();
                            CollectionReference attendanceRef = db.collection("attendance");
                            Timestamp check = new Timestamp(System.currentTimeMillis());
                            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                            String formattedTime = sdf.format(check);
                            SimpleDateFormat sdf1 = new SimpleDateFormat("d MMM YY");
                            String formattedDate = sdf1.format(check);
                            attendance_model = new Attendance_model("test@gmail.com", formattedDate, "Checked In", formattedDate, R.drawable.checkin);
                            attendance_model.setLocationsModel(currentLocation);
                            System.out.println("time : " + formattedTime);
                            viewHolder.time.setText(formattedTime);
                            attendanceRef
                                    .add(attendance_model)
                                    .addOnSuccessListener(documentReference -> {
                                    })
                                    .addOnFailureListener(e -> {
                                    });
                            //show the place:
                            ShowAttendanceLocation yourFragment = new ShowAttendanceLocation();
                            Bundle args = new Bundle();
                            args.putSerializable("loc", (Serializable) currentLocation);
                            args.putSerializable("attendance", (Serializable) attendance_model);
                            yourFragment.setArguments(args);
                            fragmentManager.beginTransaction()
                                    .replace(R.id.content, yourFragment, "location")
                                    .addToBackStack("location")
                                    .commit();
                            checkedin = true;

//                        }
                            return;
                        }
                    }
                    if (att_flag == 0)
                        Toast.makeText(context, "Outside Geofencing location. If theres an issue call the HR.", Toast.LENGTH_SHORT).show();
                }

        @Override
        protected Void doInBackground(Void... voids) {
            dataList = new ArrayList<LocationsModel>();
            System.out.println("coming here....");
            CollectionReference collectionRef = db.collection("cities");
            Task<QuerySnapshot> task = collectionRef.get();
            task.addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    QuerySnapshot querySnapshot = task1.getResult();
                    if (querySnapshot != null) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            LocationsModel model = document.toObject(LocationsModel.class);
                            boolean isInside = PolyUtil.containsLocation(userLocation, convertPointsToLatLngList(model.getPolygon()), true);
                            if(isInside){
                                System.out.println("location inside : "+model.getName());
                                currentLocation = model;
                                break;
                            }
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
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
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
                        Toast.makeText(context, "Failed to get location", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Permission is not yet granted
            // Request the permission
//            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
}
