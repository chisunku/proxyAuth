package com.example.checking;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Authentication extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        //check if shared preference is empty
        //if yes then show the login page
        //else show 2 buttons like discover app where the user can login with email or biometric
        // Retrieve SharedPreferences
        sharedPreferences = getPreferences(MODE_PRIVATE);

        // Check if SharedPreferences is empty
        if (isSharedPreferencesEmpty()) {
            // Shared preferences are empty, show login page
            // For simplicity, let's start a LoginActivity (replace with your login activity)
            Intent loginIntent = new Intent(this, LoginWithEmail.class);
            startActivity(loginIntent);
            finish(); // Finish the current activity to prevent the user from navigating back
        } else {
            // Shared preferences are not empty, show buttons for email and biometric login
            setupButtons();
        }


    }

    private boolean isSharedPreferencesEmpty() {
        // Check if any data is stored in shared preferences
        return sharedPreferences.getAll().isEmpty();
    }

    private void setupButtons() {
        // Initialize buttons and set click listeners
        Button btnLoginWithEmail = findViewById(R.id.login);
        Button btnLoginWithBiometric = findViewById(R.id.biometric);

        btnLoginWithEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle email login button click
                // Implement your logic to navigate to the email login screen
                Intent loginIntent = new Intent(getApplicationContext(), LoginWithEmail.class);
                startActivity(loginIntent);
                finish();
            }
        });

        btnLoginWithBiometric.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle biometric login button click
                // Implement your logic to initiate biometric authentication
            }
        });
    }

}
