package com.example.checking;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;
import org.locationtech.jts.triangulate.quadedge.QuadEdgeSubdivision;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Boundary extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<LatLng> polygonPoints = new ArrayList<>();
    private Polygon polygon;
    private ZoomControls zoomControls;
    private AutoCompleteTextView autoCompleteTextView;
    private FirebaseFirestore db;

    private AutocompleteSupportFragment autocompleteFragment;
    View view;

    private void initializePlaces() {
        if (!Places.isInitialized()) {
            Places.initialize(getContext(), "AIzaSyAHNqB-5OeXeVss95CwnVO7IFjKbJe7mzE");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_maps, parent, false);
        db = FirebaseFirestore.getInstance();
        initializePlaces();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button saveBtn = view.findViewById(R.id.btnSave);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePolygon();
            }
        });

        // Initialize the AutocompleteSupportFragment.
//        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
//                 fragmentManager.findFragmentById(R.id.autocomplete_fragment);

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,  Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                Log.i("Places_auto_complete", "Place: " + place.getName() + ", " + place.getId()+", "+place.getLatLng());
                if (place != null && place.getLatLng() != null) {
                    Toast.makeText(getContext(), "in if"+ place.getLatLng(), Toast.LENGTH_SHORT).show();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
                }
                else{
                    Toast.makeText(getContext(), "in else", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i("Places_auto_complete", "An error occurred: " + status);
            }
        });
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng defaultLocation = new LatLng(37.7749, -122.4194);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));
        UiSettings uiSettings = mMap.getUiSettings();
        // Enable or disable desired UI settings
        uiSettings.setZoomControlsEnabled(true); // Show zoom controls
        uiSettings.setCompassEnabled(true); // Show compass
        uiSettings.setMyLocationButtonEnabled(true); // Show my location button
        mMap.setOnMapClickListener(latLng -> {
            // Add the clicked point to the polygon
            polygonPoints.add(latLng);
            Log.d("TAG", "polygone looks like : "+polygonPoints);
            // Draw the Delaunay triangulation polygon
            drawDelaunayPolygon();
        });
    }

//    private void savePolygon() {
//        // Convert polygonPoints (LatLng) to a list of custom objects or use a Map
//        List<Map<String, Double>> polygonDataList = new ArrayList<>();
//        for (LatLng point : polygonPoints) {
//            Map<String, Double> pointData = new HashMap<>();
//            pointData.put("latitude", point.latitude);
//            pointData.put("longitude", point.longitude);
//            polygonDataList.add(pointData);
//        }
//
//        Map<String, Object> location = new HashMap<>();
//        TextInputLayout locationName = view.findViewById(R.id.Name);
//        TextInputLayout locationAddress = view.findViewById(R.id.Address);
//        String address = String.valueOf(locationAddress.getEditText().getText());
//        location.put("name", String.valueOf(locationName.getEditText().getText()));
//        location.put("address", address);
//        location.put("polygon", polygonDataList);
//
//        db.collection("cities").document(address)
//                .set(location)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.d("DB insert", "DocumentSnapshot successfully written!");
//                        Toast.makeText(getContext(), "New location added!", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w("DB insert", "Error writing document", e);
//                        Toast.makeText(getContext(), "Failed to add new location. Please try again!", Toast.LENGTH_SHORT).show();
//                    }
//                });
//        LocationListView fragment = new LocationListView();
//        FragmentManager fragmentManager = getFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.replace(R.id.content, fragment, "");
//        fragmentTransaction.addToBackStack("location");
//        fragmentTransaction.commit();
//    }

    private void savePolygon() {
        if (polygonPoints.size() > 2) {
            // Convert polygonPoints (LatLng) to a list of custom objects or use a Map
            List<Map<String, Double>> polygonDataList = new ArrayList<>();
            for (LatLng point : polygon.getPoints()) {
                Map<String, Double> pointData = new HashMap<>();
                pointData.put("latitude", point.latitude);
                pointData.put("longitude", point.longitude);
                polygonDataList.add(pointData);
            }

            Map<String, Object> location = new HashMap<>();
            TextInputLayout locationName = view.findViewById(R.id.Name);
            TextInputLayout locationAddress = view.findViewById(R.id.Address);
            String address = String.valueOf(locationAddress.getEditText().getText());
            location.put("name", String.valueOf(locationName.getEditText().getText()));
            location.put("address", address);
            location.put("polygon", polygonDataList);

            db.collection("cities").document(address)
                    .set(location)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("DB insert", "DocumentSnapshot successfully written!");
                            Toast.makeText(getContext(), "New location added!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("DB insert", "Error writing document", e);
                            Toast.makeText(getContext(), "Failed to add new location. Please try again!", Toast.LENGTH_SHORT).show();
                        }
                    });
            LocationListView fragment = new LocationListView();
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content, fragment, "");
            fragmentTransaction.addToBackStack("location");
            fragmentTransaction.commit();
        } else {
            Toast.makeText(getContext(), "Please draw a valid polygon on the map before saving.", Toast.LENGTH_SHORT).show();
        }
    }


    private void drawDelaunayPolygon() {
        // Clear the existing polygon
        if (polygon != null) {
            polygon.remove();
        }
        Log.d("TAG", "polygonPoints : "+polygonPoints);
        // Draw the Delaunay triangulation polygon with current points
        if (polygonPoints.size() > 2) {
            // Create a DelaunayTriangulationBuilder and add the points
            DelaunayTriangulationBuilder dtb = new DelaunayTriangulationBuilder();
            GeometryFactory geometryFactory = new GeometryFactory();

            // Create a JTS Geometry from the points
            Coordinate[] coordinates = new Coordinate[polygonPoints.size()];
            for (int i = 0; i < polygonPoints.size(); i++) {
                LatLng point = polygonPoints.get(i);
                coordinates[i] = new Coordinate(point.longitude, point.latitude);
            }
            Geometry inputGeometry = geometryFactory.createMultiPointFromCoords(coordinates);

            // Set the input geometry for the DelaunayTriangulationBuilder
            dtb.setSites(inputGeometry);

            // Build the Delaunay triangulation
            QuadEdgeSubdivision subdiv = dtb.getSubdivision();

            // Extract the convex hull from the triangulation
            Geometry convexHull = subdiv.getEdges(new GeometryFactory()).convexHull();

            // Draw the Delaunay triangulation polygon (convex hull)
            List<LatLng> triangulationPoints = new ArrayList<>();
            for (Coordinate coordinate : convexHull.getCoordinates()) {
                LatLng point = new LatLng(coordinate.y, coordinate.x);
                triangulationPoints.add(point);
            }

            PolygonOptions polygonOptions = new PolygonOptions()
                    .addAll(triangulationPoints);

            polygon = mMap.addPolygon(polygonOptions);
        }
    }
}
