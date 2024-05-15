package com.example.checking;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.checking.Model.Employee;
import com.example.checking.Service.APIService;
import com.example.checking.Service.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginWithEmail extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_auth);
        sharedPreferences = getApplicationContext().getSharedPreferences("proxyAuth", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("UUID", null);

        ImageView logo = findViewById(R.id.icon);
        TextInputEditText email = findViewById(R.id.email);
        TextInputEditText password = findViewById(R.id.password);
        Button login = findViewById(R.id.Login);

        ConstraintLayout inputLayout = findViewById(R.id.inputLayout);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputLayout.setVisibility(View.GONE);
                logo.setForegroundGravity(View.TEXT_ALIGNMENT_CENTER);
                Boolean admin = false;
                if(email.getText().toString().equals("admin@proxyAuth.com"))
                    admin = true;
                Log.d("TAG", "onCreate: authenticate file admin : "+ admin+" "+email.getText().toString());
                APIService apiService = RetrofitClient.getClient().create(APIService.class);
                if(admin){
                    Call<Employee> call = apiService.adminEmail(email.getText().toString(), password.getText().toString());
                    Boolean finalAdmin = admin;
                    call.enqueue(new Callback<Employee>() {
                        @Override
                        public void onResponse(Call<Employee> call, Response<Employee> response) {
                            if (response.body() != null) {
                                Employee employee = response.body();
                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                i.putExtra("Employee", employee);
                                i.putExtra("admin", finalAdmin);
                                startActivity(i);
                            } else {
                                Toast.makeText(LoginWithEmail.this, "Wrong email/ password entered.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Employee> call, Throwable t) {
                            // Handle network errors
                            System.out.println("error Auth with email: " + t.fillInStackTrace());
                            // Inflate the custom layout
                            LayoutInflater inflater = getLayoutInflater();
                            View layout = inflater.inflate(R.layout.toast_layout, null);

// Set the text for the TextView in the custom layout
                            TextView textView = layout.findViewById(R.id.textView);
                            textView.setText("Error! This is not your primary phone. Please login with your primary phone or reach out to the admin.");

// Create and show the Toast
                            Toast toast = new Toast(getApplicationContext());
                            toast.setDuration(Toast.LENGTH_LONG);
                            toast.setView(layout);
                            toast.show();

//                            Toast.makeText(getApplicationContext(), "Error! This is not your primary phone. Please login with your primary phone or reach out to the admin.", Toast.LENGTH_LONG).show();
                            Intent i = new Intent(getApplicationContext(), Authentication.class);
                            startActivity(i);
                        }
                    });
                }
                else {
                    Call<Employee> call = apiService.emailAuth(email.getText().toString(), password.getText().toString(), userId);
                    System.out.println("call : ");
                    call.enqueue(new Callback<Employee>() {
                        @Override
                        public void onResponse(Call<Employee> call, Response<Employee> response) {
                            if (response.body() != null) {
                                Employee employee = response.body();
                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                Log.d("TAG", "onResponse: emp name : " + employee.getName());
                                i.putExtra("Employee", employee);
                                i.putExtra("admin", false);
                                startActivity(i);
                            } else {
                                Toast.makeText(LoginWithEmail.this, "Wrong email/ password entered.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Employee> call, Throwable t) {
                            // Handle network errors
                            System.out.println("error Auth with email: " + t.fillInStackTrace());
                            // Inflate the custom layout
                            LayoutInflater inflater = getLayoutInflater();
                            View layout = inflater.inflate(R.layout.toast_layout, null);

// Set the text for the TextView in the custom layout
                            TextView textView = layout.findViewById(R.id.textView);
                            textView.setText("Error! This is not your primary phone. Please login with your primary phone or reach out to the admin.");

// Create and show the Toast
                            Toast toast = new Toast(getApplicationContext());
                            toast.setDuration(Toast.LENGTH_LONG);
                            toast.setView(layout);
                            toast.show();
//                            Toast.makeText(getApplicationContext(), "Error! This is not your primary phone. Please login with your primary phone or reach out to the admin.", Toast.LENGTH_LONG).show();
                            Intent i = new Intent(getApplicationContext(), Authentication.class);
                            startActivity(i);
                        }
                    });
                }
            }
        });

    }
}
