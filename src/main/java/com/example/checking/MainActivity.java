package com.example.checking;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.checking.Model.Attendance;
import com.example.checking.Model.Employee;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity{

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    final int REQUEST_CODE = 101;

    BottomNavigationView navigationView = null;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;

    private LatLng userLocation;

    Employee employee;
    Boolean admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        employee = (Employee) intent.getSerializableExtra("Employee");
        admin = intent.getBooleanExtra("admin", false);

        Log.d("mainActivity", "onCreate: employee name : "+employee.getName());
        navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setOnNavigationItemSelectedListener(navListener);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if(admin){
            navigationView.inflateMenu(R.menu.bottom_nav_admin);
            Bundle bundle = new Bundle();
            bundle.putBoolean("admin", admin);
            LocationListView fragment = new LocationListView();
            fragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content, fragment, "");
            fragmentTransaction.addToBackStack("location");
            fragmentTransaction.commit();
        }
        else {
            navigationView.inflateMenu(R.menu.bottom_nav);
            HomeFragment fragment = new HomeFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("Employee", employee);
            fragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content, fragment, "");
            fragmentTransaction.commit();
        }
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
            try {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else{
            Toast.makeText(this, "Your device is not suitable for this app", Toast.LENGTH_SHORT).show();
            finish();
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

        if (requestCode == REQUEST_CODE) {
            // in the below line, we are checking if permission is granted.
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // if permissions are granted we are displaying below toast message.
                Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                // in the below line, we are displaying toast message
                // if permissions are not granted.
                Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        int itemId = item.getItemId();
        if(itemId == R.id.home){
            if(admin){
                LocationListView fragment = new LocationListView();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content, fragment, "main");
                fragmentTransaction.commit();
            }
            else {
                HomeFragment fragment = new HomeFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("Employee", employee);
                bundle.putBoolean("admin", admin);
                fragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content, fragment, "");
                fragmentTransaction.addToBackStack("home");
                fragmentTransaction.commit();
            }
            return true;
        } else if (itemId == R.id.location) {
            LocationListView fragment = new LocationListView();
            Bundle bundle = new Bundle();
            Log.d("TAG", "admin bool flag for location : "+admin);
            bundle.putBoolean("admin", admin);
            fragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content, fragment, "");
            fragmentTransaction.addToBackStack("location");
            fragmentTransaction.commit();
            return true;
        }
        else if (itemId == R.id.leave) {
            LeavesFragment fragment = new LeavesFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("Employee", employee);
            bundle.putBoolean("admin", admin);
            fragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content, fragment, "");
            fragmentTransaction.addToBackStack("leave");
            fragmentTransaction.commit();
            return true;
        }
        else if (itemId == R.id.location) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("admin", admin);
            LocationListView fragment = new LocationListView();
            fragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content, fragment, "");
            fragmentTransaction.addToBackStack("location");
            fragmentTransaction.commit();
            return true;
        }
        else if(itemId == R.id.profile){
            Log.d("TAG", "in admin profile : "+admin);
            if(admin){
                Log.d("TAG", "in admin profile : "+admin);
                EmpView fragment = new EmpView();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content, fragment, "");
                fragmentTransaction.addToBackStack("profile");
                fragmentTransaction.commit();
                return true;
            }
            else {
                Profile fragment = new Profile();
                Bundle bundle = new Bundle();
                bundle.putSerializable("employee", employee);
                fragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content, fragment, "");
                fragmentTransaction.addToBackStack("profile");
                fragmentTransaction.commit();
                return true;
            }
        }
        else if(itemId == R.id.logout){
            Intent intent = new Intent(MainActivity.this, Authentication.class);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    };
}
