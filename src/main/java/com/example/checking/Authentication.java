package com.example.checking;

import static androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED;
import static androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.checking.Model.Employee;
import com.example.checking.Service.APIService;
import com.example.checking.Service.RetrofitClient;

import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Authentication extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;
    String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        //check if shared preference is empty
        //if yes then show the login page
        //else show 2 buttons like discover app where the user can login with email or biometric
        // Retrieve SharedPreferences
        sharedPreferences = getApplicationContext().getSharedPreferences("proxyAuth", Context.MODE_PRIVATE);

        // Check if SharedPreferences is empty
        if (isSharedPreferencesEmpty()) {
            // Shared preferences are empty, show login page
            // For simplicity, let's start a LoginActivity (replace with your login activity)
            Intent loginIntent = new Intent(this, LoginWithEmail.class);
            startActivity(loginIntent);
            finish(); // Finish the current activity to prevent the user from navigating back
        } else {
            // Shared preferences are not empty, show buttons for email and biometric login
            userId = sharedPreferences.getString("UUID", null);
            setupButtons();
            //fetch the shared preferences

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
                BiometricManager biometricManager = BiometricManager.from(getApplicationContext());
                switch(biometricManager.canAuthenticate()){
                    case BIOMETRIC_ERROR_NO_HARDWARE:
                        Toast.makeText(getApplicationContext(), "Biomertric authenticator not available on your device", Toast.LENGTH_SHORT).show();
                        break;

                    case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                        Toast.makeText(getApplicationContext(), "HW unawailable", Toast.LENGTH_SHORT).show();
                        break;

                    case BIOMETRIC_ERROR_NONE_ENROLLED:
                        Toast.makeText(getApplicationContext(), "No biometric assigned", Toast.LENGTH_SHORT).show();
                        break;
                }

                Executor executor = ContextCompat.getMainExecutor(getApplicationContext());

                biometricPrompt = new BiometricPrompt(Authentication.this, executor, new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Toast.makeText(getApplicationContext(), "Login Successful!", Toast.LENGTH_SHORT).show();

                        //get employee dets
                        APIService apiService = RetrofitClient.getClient().create(APIService.class);
                        Call<Employee> call = apiService.userIdAuth(userId);
                        Log.d("TAG", "onAuthenticationSucceeded: call");
                        call.enqueue(new Callback<Employee>() {
                            @Override
                            public void onResponse(Call<Employee> call, Response<Employee> response) {
                                if(response.body()!=null){
                                    Employee employee = response.body();
                                    Log.d("TAG", "onResponse: emp name : "+employee.getName());
                                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                    i.putExtra("Employee", employee);
                                    startActivity(i);
                                }
                                else{
                                    Toast.makeText(Authentication.this, "Wrong email/ password entered.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Employee> call, Throwable t) {
                                // Handle network errors
                                System.out.println("error Auth with email: " + t.fillInStackTrace());
                            }
                        });
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                    }
                });

                promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle("Checkin at ProxyAuth!")
                        .setDescription("Use FingerPrint to Login..")
                        .setDeviceCredentialAllowed(true).build();

                biometricPrompt.authenticate(promptInfo);
            }
        });
    }

}
