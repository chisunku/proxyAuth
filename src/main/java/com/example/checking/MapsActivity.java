//package com.example.checking;
//
//import android.content.DialogInterface;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.Polygon;
//import com.google.android.gms.maps.model.PolygonOptions;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
//
//    private GoogleMap mMap;
//    private List<LatLng> boundaryPoints = new ArrayList<>();
//    private Polygon drawnPolygon;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_maps);
//
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_save:
//                // Handle the save action
//                saveBoundary();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//
//        // Move the camera to a default location (e.g., your city or a specific location)
//        LatLng defaultLocation = new LatLng(37.7749, -122.4194);
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));
//
//        // Set up drawing functionality
//        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(LatLng latLng) {
//                addBoundaryPoint(latLng);
//            }
//        });
//    }
//
//    private void addBoundaryPoint(LatLng point) {
//        boundaryPoints.add(point);
//
//        if (drawnPolygon != null) {
//            drawnPolygon.remove();
//        }
//
//        // Draw the polygon with the updated points
//        PolygonOptions polygonOptions = new PolygonOptions()
//                .addAll(boundaryPoints)
//                .strokeColor(Color.RED)
//                .fillColor(Color.argb(128, 255, 0, 0)); // Semi-transparent red fill
//
//        drawnPolygon = mMap.addPolygon(polygonOptions);
//    }
//
//    private void saveBoundary() {
//        if (boundaryPoints.size() < 3) {
//            // A polygon must have at least 3 points
//            Toast.makeText(this, "Please select at least 3 boundary points", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Do something with the boundary points, e.g., save to a database
//        showSaveDialog();
//    }
//
//    private void showSaveDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Save Boundary");
//        builder.setMessage("Do you want to save the selected boundary?");
//        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                // Implement the save logic here
//                // For example, you can save the boundary points to a database
//                Toast.makeText(MapsActivity.this, "Boundary saved!", Toast.LENGTH_SHORT).show();
//            }
//        });
//        builder.setNegativeButton("Cancel", null);
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }
//}
//
