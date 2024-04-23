package com.example.checking;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.checking.Model.Employee;

public class LocationService extends Service {

    private static final String TAG = "ForegroundService";
    private static final int NOTIFICATION_ID = 123;

    private LocationManager locationManager;
    Employee employee;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: starting service");
        Bundle bundle = intent.getExtras();
        employee = (Employee) bundle.getSerializable("Employee");
        createNotificationChannel();
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
                    5*60*1000, 0, locationListener, Looper.getMainLooper());
            Log.d(TAG, "getLocation: inside getLocation if");
            // Check for the last known location
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                Log.d(TAG, "Last known location: " + lastKnownLocation.getLatitude()+" "+lastKnownLocation.getLongitude());
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
            Log.d(TAG, "onLocationChanged: " + location.toString());
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
        NotificationCompat.Builder builder = new
                NotificationCompat.Builder(this, "10")
                .setContentTitle("Location Service")
                .setContentText("Getting location updates")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setOngoing(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d(TAG, "getNotification: inside notification if condition");
        }
        return builder.build();
    }

}
