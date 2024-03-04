package com.example.checking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.checking.Service.APIService;
import com.example.checking.Service.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginWithEmail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_auth);
        TextInputEditText email = findViewById(R.id.email);
        TextInputEditText password = findViewById(R.id.password);

        Button login = findViewById(R.id.Login);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                APIService apiService = RetrofitClient.getClient().create(APIService.class);
                Call<Boolean> call = apiService.emailAuth(email.getText().toString(), password.getText().toString());
                System.out.println("call : ");
                call.enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        if(response.body()){
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);
                        }
                        else{
                            Toast.makeText(LoginWithEmail.this, "Wrong email/ password entered.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Boolean> call, Throwable t) {
                        // Handle network errors
                        System.out.println("error Auth with email: " + t.fillInStackTrace());
                    }
                });
            }
        });

    }
}
