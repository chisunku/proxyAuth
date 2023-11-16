package com.example.checking;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;
import org.locationtech.jts.triangulate.quadedge.QuadEdgeSubdivision;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<LatLng> polygonPoints = new ArrayList<>();
    private Polygon polygon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Inside MainActivity or Application class
        FirebaseApp.initializeApp(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save the polygon if it is completely connected
                if (isCompletelyConnected()) {
                    savePolygon();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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
        // Assuming you have a Firebase project set up and initialized
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("polygons");
//
//        // Convert polygonPoints (LatLng) to a list of custom objects or use a Map
//        List<Map<String, Double>> polygonDataList = new ArrayList<>();
//        for (LatLng point : polygonPoints) {
//            Map<String, Double> pointData = new HashMap<>();
//            pointData.put("latitude", point.latitude);
//            pointData.put("longitude", point.longitude);
//            polygonDataList.add(pointData);
//        }
//
//        // Generate a unique key for the polygon
//        String polygonKey = databaseReference.push().getKey();
//
//        // Save the polygon data to Firebase
//        if (polygonKey != null) {
//            databaseReference.child(polygonKey).setValue(polygonDataList)
//                    .addOnSuccessListener(aVoid -> {
//                        // Successfully saved to Firebase
//                        Toast.makeText(this, "Polygon saved to Firebase", Toast.LENGTH_SHORT).show();
//                    })
//                    .addOnFailureListener(e -> {
//                        // Failed to save to Firebase
//                        Toast.makeText(this, "Failed to save polygon to Firebase", Toast.LENGTH_SHORT).show();
//                    });
//        }
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        DocumentReference mapReference = db.collection("proxyAuth").document("Office_Location");

        // Get reference to Firestore collection
        CollectionReference polygonCollection = FirebaseFirestore.getInstance().collection("proxyAuth");

// Convert polygonPoints (LatLng) to a list of custom objects or use a Map
        List<Map<String, Double>> polygonDataList = new ArrayList<>();
        for (LatLng point : polygonPoints) {
            Map<String, Double> pointData = new HashMap<>();
            pointData.put("latitude", point.latitude);
            pointData.put("longitude", point.longitude);
            polygonDataList.add(pointData);
        }

// Generate a unique key for the polygon (optional in Firestore)
// String polygonKey = UUID.randomUUID().toString(); // You can use this if you want to generate a key manually

// Save the polygon data to Firestore
        polygonCollection.document()
                .set(new HashMap<String, Object>() {{
                    put("points", polygonDataList);
                }}, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // Successfully saved to Firestore
                    Toast.makeText(this, "Polygon saved to Firestore", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Failed to save to Firestore
                    Toast.makeText(this, "Failed to save polygon to Firestore", Toast.LENGTH_SHORT).show();
                });

    }


    private boolean isCompletelyConnected() {
        // Check if the Delaunay triangulation is completely connected
        // In this example, we assume the Delaunay triangulation is completely connected if it has at least 3 points
        return polygonPoints.size() >= 3;
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
                    .addAll(triangulationPoints)
                    .strokeWidth(5)
                    .strokeColor(Color.RED)
                    .fillColor(Color.BLUE);

            polygon = mMap.addPolygon(polygonOptions);
        }
    }
}
