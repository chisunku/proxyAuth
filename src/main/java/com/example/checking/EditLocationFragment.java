package com.example.checking;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.checking.Model.Location;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditLocationFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    List<Location.Point> polygon;
    Location loc;
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_location, parent, false);
        Bundle args = getArguments();
        loc = (Location) args.getSerializable("loc");
        System.out.println("Name: "+loc.getName());
        polygon = loc.getPolygon();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
            // Draw polygon on the map
            drawPolygon(polygon);

            // Move camera to the bounds of the polygon
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(getPolygonBounds(polygon), 50));
    }

    private void drawPolygon(List<Location.Point> polygonPoints) {
        if (polygonPoints.size() >= 3) {
            List<LatLng> latLngs = sortPointsClockwise(polygonPoints);

            // Draw the polygon on the map
            PolygonOptions polygonOptions = new PolygonOptions()
                    .addAll(latLngs)
                    .strokeColor(Color.RED)
                    .fillColor(Color.argb(100, 255, 0, 0));
            Polygon polygon1 = mMap.addPolygon(polygonOptions);
        }
    }

    private List<LatLng> sortPointsClockwise(List<Location.Point> points) {
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



    private LatLngBounds getPolygonBounds(List<Location.Point> polygonPoints) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Location.Point point : polygonPoints) {
            builder.include(new LatLng(point.getLatitude(), point.getLongitude()));
        }
        return builder.build();
    }


}
