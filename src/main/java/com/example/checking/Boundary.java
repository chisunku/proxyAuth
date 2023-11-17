package com.example.checking;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ZoomControls;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
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

public class Boundary extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<LatLng> polygonPoints = new ArrayList<>();
    private Polygon polygon;
    private ZoomControls zoomControls;
    private AutoCompleteTextView autoCompleteTextView;
    private FirebaseFirestore db;

    private AutocompleteSupportFragment autocompleteFragment;


    private void initializePlaces() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyAHNqB-5OeXeVss95CwnVO7IFjKbJe7mzE");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        db = FirebaseFirestore.getInstance();
        initializePlaces();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button saveBtn = findViewById(R.id.btnSave);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePolygon();
            }
        });

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,  Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                Log.i("Places_auto_complete", "Place: " + place.getName() + ", " + place.getId()+", "+place.getLatLng());
                if (place != null && place.getLatLng() != null) {
                    Toast.makeText(Boundary.this, "in if"+ place.getLatLng(), Toast.LENGTH_SHORT).show();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 10));
                }
                else{
                    Toast.makeText(Boundary.this, "in else", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i("Places_auto_complete", "An error occurred: " + status);
            }
        });



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);

        LatLng defaultLocation = new LatLng(37.7749, -122.4194);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));

        mMap.setOnMapClickListener(latLng -> {
            // Add the clicked point to the polygon
            polygonPoints.add(latLng);

            // Draw the Delaunay triangulation polygon
            drawDelaunayPolygon();
        });
    }

    private void savePolygon() {
        // Convert polygonPoints (LatLng) to a list of custom objects or use a Map
        List<Map<String, Double>> polygonDataList = new ArrayList<>();
        for (LatLng point : polygonPoints) {
            Map<String, Double> pointData = new HashMap<>();
            pointData.put("latitude", point.latitude);
            pointData.put("longitude", point.longitude);
            polygonDataList.add(pointData);
        }

        Map<String, Object> location = new HashMap<>();
        location.put("name", "SFO");
        location.put("polygon", polygonDataList);

        db.collection("cities").document("LA")
                .set(location)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("DB insert", "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DB insert", "Error writing document", e);
                    }
                });
    }

    private void drawDelaunayPolygon() {
        // Clear the existing polygon
        if (polygon != null) {
            polygon.remove();
        }

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
