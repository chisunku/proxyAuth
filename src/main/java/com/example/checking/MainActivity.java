package com.example.checking;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.core.Bound;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;
import org.locationtech.jts.triangulate.quadedge.QuadEdgeSubdivision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.android.libraries.places.api.Places;
import com.google.maps.android.PolyUtil;


public class MainActivity extends AppCompatActivity{

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private List<LatLng> polygonPoints = new ArrayList<>();

    ArrayList<LocationsModel> dataList = new ArrayList<>();

    BottomNavigationView navigationView = null;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private FirebaseFirestore db;

    private LatLng userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setOnNavigationItemSelectedListener(navListener);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        HomeFragment fragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content, fragment, "");
        fragmentTransaction.commit();
    }

    private void enableMyLocation() {
            // Zoom to the user's current location if available
            requestLastLocation();
            startLocationUpdates();
    }

    private void requestLastLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // 5 seconds
        locationRequest.setFastestInterval(2000); // 2 seconds

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        int itemId = item.getItemId();
        if(itemId == R.id.home){
            HomeFragment fragment = new HomeFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content, fragment, "");
            fragmentTransaction.addToBackStack("home");
            fragmentTransaction.commit();
            return true;
        } else if (itemId == R.id.location) {
            LocationListView fragment = new LocationListView();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content, fragment, "");
            fragmentTransaction.addToBackStack("location");
            fragmentTransaction.commit();
            return true;
        }
        // It will help to replace the
        // one fragment to other.
//        if (selectedFragment != null) {
//            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
//        }
        return false;
    };

    public void updateBottomNavigation(int itemId) {
        navigationView.setSelectedItemId(itemId);
    }

}
