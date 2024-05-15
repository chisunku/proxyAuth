package com.example.checking;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.checking.Model.Employee;
import com.example.checking.Model.Location;
import com.example.checking.Service.APIService;
import com.example.checking.Service.RetrofitClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.handler>{

    private final Context context;
    public List<Location> locationModelArrayList;
    FragmentManager fragmentManager;

    Boolean admin;

    // Constructor
    public LocationAdapter(Context context, List<Location> locationModelArrayList, FragmentManager fragmentManager, Boolean admin) {
        this.context = context;
        this.locationModelArrayList = locationModelArrayList;
        this.fragmentManager = fragmentManager;
        this.admin = admin;
    }

    @NonNull
    @Override
    public LocationAdapter.handler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // to inflate the layout for each item of recycler view.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_location, parent, false);
        return new handler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationAdapter.handler holder, int position) {
        // to set data to textview and imageview of each card layout
        System.out.println("Location adapter"+locationModelArrayList.size());
        Location model = locationModelArrayList.get(position);
        holder.locationAddress.setText(model.getAddress());
        holder.locationCity.setText("" + model.getName());
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.map.getVisibility() == View.GONE) {
                        //view map dynamically
                        holder.map.setVisibility(View.VISIBLE);
                        LiveTracking(model, holder.map);
                    } else {
                        //hide map
                        holder.map.setVisibility(View.GONE);
                    }
                }
            });
    }

    GoogleMap mMap;
    List<Location.Point> polygon;
    APIService apiService;

    public void LiveTracking(Location model, MapView mapView) {
        Log.d("TAG", "LiveTracking: started!");
        polygon = model.getPolygon();
        apiService = RetrofitClient.getClient().create(APIService.class);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                drawPolygon(polygon);
                if(admin) {
                    addLocationMarkers();
                }
            }
        });
    }

    private void addLocationMarkers() {
        Call<List<Employee>> call = apiService.getAllEmployees();
        call.enqueue(new Callback<List<Employee>>() {
            @Override
            public void onResponse(Call<List<Employee>> call, Response<List<Employee>> response) {
                if (response.isSuccessful()) {
                    List<Employee> employees = response.body();
                    for (Employee employee : employees) {
                        if (employee.getLatitude() != 0 && employee.getLongitude() != 0) {
                            LatLng latLng = new LatLng(employee.getLatitude(), employee.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(latLng).title(employee.getName()));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Employee>> call, Throwable throwable) {
                Log.e("Error", "Failed to fetch employees: " + throwable.getMessage());
            }
        });
    }

    private void drawPolygon(List<Location.Point> polygonPoints) {
        if (mMap == null || polygonPoints == null || polygonPoints.size() < 3) {
            return;
        }

        List<LatLng> latLngs = sortPointsClockwise(polygonPoints);

        // Draw the polygon on the map
        PolygonOptions polygonOptions = new PolygonOptions()
                .addAll(latLngs)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(100, 255, 0, 0));
        mMap.addPolygon(polygonOptions);

        // Move camera to show the polygon bounds
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(getPolygonBounds(polygonPoints), 50));
    }

    private static List<LatLng> sortPointsClockwise(List<Location.Point> points) {
        // Calculate the centroid of the points
        double cx = 0, cy = 0;
        for (Location.Point point : points) {
            cx += point.getLatitude();
            cy += point.getLongitude();
        }
        cx /= points.size();
        cy /= points.size();

        // Sort points based on the angle from the centroid
        double finalCy = cy;
        double finalCx = cx;
        Collections.sort(points, (p1, p2) -> {
            double angle1 = Math.atan2(p1.getLongitude() - finalCy, p1.getLatitude() - finalCx);
            double angle2 = Math.atan2(p2.getLongitude() - finalCy, p2.getLatitude() - finalCx);
            return Double.compare(angle1, angle2);
        });

        // Convert to LatLng and return
        List<LatLng> sortedLatLngs = new ArrayList<>();
        for (Location.Point point : points) {
            sortedLatLngs.add(new LatLng(point.getLatitude(), point.getLongitude()));
        }
        return sortedLatLngs;
    }



    private static LatLngBounds getPolygonBounds(List<Location.Point> polygonPoints) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Location.Point point : polygonPoints) {
            builder.include(new LatLng(point.getLatitude(), point.getLongitude()));
        }
        return builder.build();
    }

    @Override
    public int getItemCount() {
        // this method is used for showing number of card items in recycler view
        return locationModelArrayList.size();
    }

    public static class handler extends RecyclerView.ViewHolder{
        private final TextView locationAddress;
        private final TextView locationCity;
        private CardView cardView;
        MapView map;

        public handler(View itemView) {
            super(itemView);
            locationCity = itemView.findViewById(R.id.locationName);
            locationAddress = itemView.findViewById(R.id.locationAddress);
            cardView = itemView.findViewById(R.id.cardView);
            map = itemView.findViewById(R.id.map);
            map.onCreate(null);
        }
    }
}
