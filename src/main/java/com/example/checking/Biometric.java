package com.example.checking;

import static androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED;
import static androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import com.example.checking.Model.Employee;
import com.example.checking.Service.APIService;
import com.example.checking.Service.RetrofitClient;

import java.util.UUID;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Biometric extends AppCompatActivity {

    BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;

    CoordinatorLayout mMainlayout;

    private SharedPreferences sharedPreferences;
    String userId;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMainlayout = findViewById(R.id.main_layout);

        sharedPreferences = getApplicationContext().getSharedPreferences("proxyAuth", Context.MODE_PRIVATE);

        if(sharedPreferences.getAll().isEmpty()){
            Toast.makeText(this, "something went wrong try again!", Toast.LENGTH_SHORT).show();
            finish();
        }
        else{
            userId = sharedPreferences.getString("UUID", null);
            Log.d("TAG", "onCreate: UUID: "+userId);
        }

        BiometricManager biometricManager = BiometricManager.from(this);
        switch(biometricManager.canAuthenticate()){
            case BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "Biomertric authenticator not available on your device", Toast.LENGTH_SHORT).show();
                break;

            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "HW unawailable", Toast.LENGTH_SHORT).show();
                break;

            case BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(this, "No biometric assigned", Toast.LENGTH_SHORT).show();
                break;
        }

        Executor executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(Biometric.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.d("TAG", "Login error!");
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
//                Toast.makeText(Biometric.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                Log.d("TAG", "onCreate: UUID: "+userId);
                Log.d("TAG", "Login Successful!");
                //get employee dets
                APIService apiService = RetrofitClient.getClient().create(APIService.class);
                Call<Employee> call = apiService.userIdAuth(userId);
                Log.d("TAG", "onAuthenticationSucceeded: call");
                call.enqueue(new Callback<Employee>() {
                    @Override
                    public void onResponse(Call<Employee> call, Response<Employee> response) {
                        if(response.body()!=null){
                            Employee employee = response.body();
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            i.putExtra("Employee", employee);
                            startActivity(i);
                        }
                        else{
                            Toast.makeText(Biometric.this, "Wrong email/ password entered.", Toast.LENGTH_SHORT).show();
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
                Log.d("TAG", "Login failed!");
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle("Checkin at ProxyAuth!")
                .setDescription("Use FingerPrint to Login..")
                .setDeviceCredentialAllowed(true).build();

        biometricPrompt.authenticate(promptInfo);
    }
}
