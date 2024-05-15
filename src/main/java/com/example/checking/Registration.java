package com.example.checking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.checking.Model.Employee;
import com.example.checking.Service.APIService;
import com.example.checking.Service.RetrofitClient;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.net.MalformedURLException;
import java.util.UUID;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;


public class Registration extends AppCompatActivity {

    int PICK_IMAGE_REQUEST = 1;

    private static final int REQUEST_CODE_PICK_IMAGE = 2;
    TextView name, email, password, address;

    FloatingActionButton displayPicture;
    ExtendedFloatingActionButton faceReg;
    Employee employee;
//    private SharedPreferences sharedPreferences;
//    String userId;
    MultipartBody.Part body;
    File file;
    RequestBody requestFile;
    ImageView profile_image;
    Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        employee = new Employee();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    10);
        }

        // Check if camera permission is granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    3);
        } else {
            // Permission is already granted, proceed with your code
        }

        //set the shared preference
//        sharedPreferences = getApplicationContext().getSharedPreferences("proxyAuth", Context.MODE_PRIVATE);


        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        address = findViewById(R.id.address);
        profile_image = findViewById(R.id.profile_image);

        final Intent[] galleryIntent = new Intent[1];
        displayPicture = findViewById(R.id.display_picture);
        displayPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG", "onClick: display image selection");
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
            }
        });

        faceReg = findViewById(R.id.faceRegister);
        if(name.getText().toString()!=null && email.getText().toString()!=null && password.getText().toString()!=null && address.getText().toString()!=null){
            faceReg.setVisibility(View.VISIBLE);
        }
        faceReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                employee.setName(name.getText().toString());
                employee.setEmail(email.getText().toString());
                employee.setPassword(password.getText().toString());
                employee.setAddress(address.getText().toString());
//                Log.d("registration", "onClick: url string: "+ galleryIntent[0].getData());
//                Bitmap imageBitmap;
//                try {
//                    imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), galleryIntent[0].getData());
//                    employee.setImageURL(imageBitmap);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                Log.d("TAG", "onClick: Registration: Registrationface : "+employee.getEmail());
                Intent intent = new Intent(getApplicationContext(), RegisterFace.class);
                intent.putExtra("Employee", employee);
                intent.putExtra("fileAbsolutePath", file.getAbsolutePath());
                intent.putExtra("imageURI", imageUri.toString());
                startActivityForResult(intent, 2);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            // Retrieve the URI of the selected image
            imageUri = data.getData();
            profile_image.setImageURI(imageUri);
            if (imageUri != null) {
                // Convert the image URI to a multipart file
                MultipartBody.Part imagePart = convertUriToMultipartFile(imageUri);
//                Toast.makeText(this, "the imagepart is : "+imagePart.toString(), Toast.LENGTH_SHORT).show();
                if (imagePart != null) {
                    // Image converted successfully, you can now use it in your API request
                    // For example, you can pass it to a Retrofit API call
                    Log.d("TAG", "onActivityResult: uri : "+imageUri+" "+imagePart.toString());
                    uploadImage(imagePart);
                } else {
                    Toast.makeText(this, "Failed to convert image to multipart file", Toast.LENGTH_SHORT).show();
                }
            }
        }

//        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
//            if(sharedPreferences.getAll().isEmpty()){
//                userId = UUID.randomUUID().toString();
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                editor.putString("UUID", userId);
//                editor.apply();
//                employee.setUserId(userId);
//            }
//        }
    }

    private MultipartBody.Part convertUriToMultipartFile(Uri uri) {
        try {
            // Create a file object from the URI
            file = new File(getRealPathFromURI(uri));

            // Create a request body from the file
            RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(uri)), file);

            // Create a multipart body part from the request body
            return MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        } catch (Exception e) {
            Log.e("MainActivity", "Error converting URI to multipart file: " + e.getMessage());
            return null;
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    private void uploadImage(MultipartBody.Part imagePart) {
        Log.d("TAG", "uploadImage: in upload : "+imagePart.body().toString());
        APIService apiService = RetrofitClient.getClient().create(APIService.class);

//        Call<String> test = apiService.test();
//
//        Call<Employee> call = apiService.registerEmployee(employee, imagePart);
//        call.enqueue(new Callback<Employee>() {
//            @Override
//            public void onResponse(Call<Employee> call, Response<Employee> response) {
//                Log.d("TAG", "onResponse: called! "+response.body()+" "+call.toString());
//                Toast.makeText(getApplicationContext(), "called!!", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onFailure(Call<Employee> call, Throwable t) {
//                Log.d("TAG", "onResponse: failed! "+t.getMessage()+" "+t.getStackTrace()+" "+t.fillInStackTrace());
//                Toast.makeText(getApplicationContext(), "FAiled!!", Toast.LENGTH_SHORT).show();
//            }
//        });
    }
}
