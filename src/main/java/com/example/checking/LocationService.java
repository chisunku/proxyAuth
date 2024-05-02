package com.example.checking;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.checking.Model.Employee;
import com.example.checking.Service.APIService;
import com.example.checking.Service.FragmentChangeListener;
import com.example.checking.Service.RetrofitClient;
import com.google.android.gms.maps.model.LatLng;

import org.checkerframework.checker.units.qual.C;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationService extends Service {
    APIService apiService;

    private static final String TAG = "ForegroundService";
    private static final int NOTIFICATION_ID = 123;
    String email;

    private LocationManager locationManager;

    private FragmentChangeListener fragmentChangeListener;

    public void setFragmentChangeListener(FragmentChangeListener listener) {
        this.fragmentChangeListener = listener;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: starting service");

        //read shared preferences and get email
        SharedPreferences sharedPreferences = getSharedPreferences("proxyAuth", Context.MODE_PRIVATE);
        email = sharedPreferences.getString("email", "");

        createNotificationChannel();
        apiService = RetrofitClient.getClient().create(APIService.class);
        getLocation();
        startForeground(NOTIFICATION_ID, getNotification());
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "createNotificationChannel: inside notification channel");
            NotificationChannel serviceChannel = new NotificationChannel(
                    "10",
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager =
                    getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void getLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1*60*1000, 0, locationListener, Looper.getMainLooper());
            Log.d(TAG, "getLocation: inside getLocation if");
            // Check for the last known location
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                Log.d(TAG, "Last known location: " + lastKnownLocation.toString());
            } else {
                Log.d(TAG, "Last known location is null");
            }
        } else {
            Log.d(TAG, "getLocation: Location permission not granted.");
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // Handle location updates here
            Log.d(TAG, "onLocationChanged: " + location.getLatitude() + ", " + location.getLongitude());

            //save the emp location into DB everytime
            Call<String> apiCall = apiService.updateUserLocation(email, location.getLatitude(), location.getLongitude());
            apiCall.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    Log.d(TAG, "onResponse: location response : "+response);
                    if(response!=null && response.code()==200){
                        Log.d(TAG, "onResponse: Location updated successfully");
                    }
                    else{
                        Log.d(TAG, "onResponse: Location update failed");
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable throwable) {

                }
            });


            Call<com.example.checking.Model.Location> call = apiService.checkLocation(location.getLatitude(), location.getLongitude(), "");
            call.enqueue(new Callback<com.example.checking.Model.Location>() {
                @Override
                public void onResponse(Call<com.example.checking.Model.Location> call, Response<com.example.checking.Model.Location> response) {
                    if(response!=null && response.code()==200 && response.body()!=null){
                        Toast.makeText(LocationService.this, "Inside location "+response.body().getName(), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(LocationService.this, "Not inside any office", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<com.example.checking.Model.Location> call, Throwable throwable) {
                    Toast.makeText(LocationService.this, "Something went wrong with fetching location. Contact Admin.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    private Notification getNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE);

        // Create a notification channel if running on Android Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "10",
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "10")
                .setContentTitle("Location Service")
                .setContentText("Service is running")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setOngoing(true);

        return builder.build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Service stopped");
        if (locationManager != null && locationListener != null && fragmentChangeListener!=null) {
            // Remove location updates to stop receiving location updates
            locationManager.removeUpdates(locationListener);
            fragmentChangeListener.checkoutFronService();
        }
    }
}
