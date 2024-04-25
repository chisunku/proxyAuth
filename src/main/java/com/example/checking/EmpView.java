package com.example.checking;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.checking.Model.Employee;
import com.example.checking.Service.APIService;
import com.example.checking.Service.RetrofitClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Multipart;

public class EmpView extends Fragment {
    APIService apiService;
    EmployeeViewAdapter adapter;
    MultipartBody.Part body;
    String TAG = "EmpView";
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.activity_emp_view, viewGroup, false);
        apiService = RetrofitClient.getClient().create(APIService.class);
        Call<List<Employee>> call = apiService.getAllEmployees();
        RecyclerView recyclerView = view.findViewById(R.id.emp_recycler);
        ProgressBar loadingProgressBar = view.findViewById(R.id.empLoading);
        call.enqueue(new Callback<List<Employee>>() {
            public void onResponse(Call<List<Employee>> call, Response<List<Employee>> response) {
                List<Employee> employees = response.body();
                Log.d("TAG", "onResponse: called admin Leaves!! " + employees);
                adapter = new EmployeeViewAdapter(getContext(), employees);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setAdapter(adapter);
                loadingProgressBar.setVisibility(View.GONE);
            }

            public void onFailure(Call<List<Employee>> call, Throwable t) {
                Log.d("TAG", "onFailure: called admin Leaves!! ");
            }
        });

        ImageView addEmp = view.findViewById(R.id.addEmp);
        addEmp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= 23) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            1001);
                }
                pickCSV();
            }
        });

        return view;
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] projection = {MediaStore.Files.FileColumns.DATA}; // Use FileColumns.DATA for CSV files
        Log.d(TAG, "getRealPathFromURI: projection :: "+projection.length);
        for(String s : projection){
            Log.d(TAG, "getRealPathFromURI: projection: "+s);
        }
        Cursor cursor = getActivity().getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor == null) {
            Log.d(TAG, "getRealPathFromURI: cursor is null :("   );
            return null;
        }
        Log.d(TAG, "getRealPathFromURI: cursor : "+cursor.toString());
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA); // Use FileColumns.DATA for CSV files
        Log.d(TAG, "getRealPathFromURI: column_index: "+column_index);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        Log.d(TAG, "getRealPathFromURI: path: " + path);
        cursor.close();
        return path;
    }

    private MultipartBody.Part convertUriToMultipartFile(Uri uri) {
        try {
            // Create a file object from the URI
            File file = new File(getRealPathFromURI(uri));

            Log.d("EmpView", "convertUriToMultipartFile: file path: " + file.getAbsolutePath());

            // Create a request body from the file
            RequestBody requestFile = RequestBody.create(MediaType.parse(getActivity().getContentResolver().getType(uri)), file);

            // Create a multipart body part from the request body
            return MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        } catch (Exception e) {
            Log.e("MainActivity", "Error converting URI to multipart file: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2712 && resultCode == RESULT_OK){
//            Uri uri = data.getData();
//            Log.d("TAG", "onActivityResult in upload PDF: "+uri);
////            MultipartBody.Part imagePart = convertUriToMultipartFile(uri);
//            File csvFile = new File(uri.getPath());
//            RequestBody csvBody = RequestBody.create(MediaType.parse("text/comma-separated-values"), csvFile);
//            MultipartBody.Part csvPart = MultipartBody.Part.createFormData("file", csvFile.getName(), csvBody);
//            if(csvPart != null) {
//                Log.d(TAG, "onActivityResult: Multipart " + csvPart.toString());
//            }
//            Call<Boolean> call = apiService.addEmployees(csvPart, "HR.csv");
//            call.enqueue(new Callback<Boolean>() {
//                @Override
//                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
//                    Log.d("TAG", "onResponse: called! "+response);
//                    Toast.makeText(getContext(), "called!!", Toast.LENGTH_SHORT).show();
//                }
//
//                @Override
//                public void onFailure(Call<Boolean> call, Throwable throwable) {
//                    Log.d("TAG", "onFailure: called! "+throwable.getMessage()+" "+call.toString());
//                    Toast.makeText(getContext(), "failed!!", Toast.LENGTH_SHORT).show();
//                }
//            });

            Uri uri = data.getData();
            Log.d("TAG", "onActivityResult in upload PDF: " + uri);

            InputStream inputStream = null;
            try {
                inputStream = getContext().getContentResolver().openInputStream(uri);
                if (inputStream != null) {
                    byte[] fileBytes = new byte[inputStream.available()];
                    inputStream.read(fileBytes);

                    File csvFile = new File(getContext().getCacheDir(), "HR.csv");
                    FileOutputStream outputStream = new FileOutputStream(csvFile);
                    outputStream.write(fileBytes);
                    outputStream.close();

                    RequestBody csvBody = RequestBody.create(MediaType.parse("text/comma-separated-values"), csvFile);
                    MultipartBody.Part csvPart = MultipartBody.Part.createFormData("file", csvFile.getName(), csvBody);

                    if (csvPart != null) {
                        Log.d(TAG, "onActivityResult: Multipart " + csvPart.toString());
                    }

                    Call<Boolean> call = apiService.addEmployees(csvPart, "HR.csv");
                    call.enqueue(new Callback<Boolean>() {
                        @Override
                        public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                            Log.d("TAG", "onResponse: called! " + response);
                            Toast.makeText(getContext(), "called!!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<Boolean> call, Throwable throwable) {
                            Log.d("TAG", "onFailure: called! " + throwable.getMessage() + " " + call.toString());
                            Toast.makeText(getContext(), "failed!!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (IOException e) {
                Log.e(TAG, "onActivityResult: Error reading file from Uri", e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "onActivityResult: Error closing input stream", e);
                    }
                }
            }

        }
    }

    private void pickCSV() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("text/comma-separated-values");
        startActivityForResult(i, 2712);
    }
}
