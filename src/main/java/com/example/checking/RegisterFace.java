package com.example.checking;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.checking.Model.Employee;
import com.example.checking.helpers.MLVideoHelperActivity;
import com.example.checking.helpers.vision.VisionBaseProcessor;
import com.example.checking.helpers.vision.recogniser.FaceRecognitionProcessor;
import com.google.mlkit.vision.face.Face;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class RegisterFace extends MLVideoHelperActivity implements FaceRecognitionProcessor.FaceRecognitionCallback {

    private Interpreter faceNetInterpreter;
    private FaceRecognitionProcessor faceRecognitionProcessor;

    private SharedPreferences sharedPreferences;

    private Face face;
    private Bitmap faceBitmap;
    private float[] faceVector;
    Employee employee;
    File file;
    MultipartBody.Part body;
    Uri imageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        employee = (Employee) intent.getSerializableExtra("Employee");
        Log.d("TAG", "onCreate: emp name in registerface : "+employee.getName());
        file = new File(intent.getStringExtra("fileAbsolutePath"));
        imageURI = Uri.parse(intent.getStringExtra("imageURI"));
        Log.d("TAG", "onCreate: image uri : "+imageURI);

        //set the shared preference
        sharedPreferences = getApplicationContext().getSharedPreferences("proxyAuth", Context.MODE_PRIVATE);

//        // Create RequestBody from file
//        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
//        // Create MultipartBody.Part object
//        body = MultipartBody.Part.createFormData("file", file.getName(), requestBody);

        RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(imageURI)), file);

        // Create a multipart body part from the request body
        body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        Log.d("TAG", "onCreate: in face rec employee email : "+employee.getEmail());
        setData(employee, body, true, true);
        super.onCreate(savedInstanceState);
    }

    //setting the pretrained tenserflow model
    @Override
    protected VisionBaseProcessor setProcessor() {
        try {
            faceNetInterpreter = new Interpreter(FileUtil.loadMappedFile(this, "mobile_face_net.tflite"), new Interpreter.Options());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("TAG", "setProcessor: Face rec email : "+ employee.getEmail());

        faceRecognitionProcessor = new FaceRecognitionProcessor(
                faceNetInterpreter,
                graphicOverlay,
                this,
                employee,
                super.empFace
        );
//        faceRecognitionProcessor.activity = this;
        return faceRecognitionProcessor;
    }

    @Override
    public void onFaceDetected(Face face, Bitmap faceBitmap, float[] faceVector) {
        this.face = face;
        this.faceBitmap = faceBitmap;
        this.faceVector = faceVector;
        Log.d("face detected", "here is where the face is detected");
    }

    @Override
    public void onFaceRecognised(Face face, float probability, String name) {

    }

    @Override
    public void onAddFaceClicked(View view) {
        super.onAddFaceClicked(view);

        if (face == null || faceBitmap == null) {
            return;
        }

        Face tempFace = face;
        Bitmap tempBitmap = faceBitmap;
        float[] tempVector = faceVector;

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.add_face_dialog, null);
        ((ImageView) dialogView.findViewById(R.id.dlg_image)).setImageBitmap(tempBitmap);

        View root = this.findViewById(android.R.id.content);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("register the user in facerec", "coming into the if");
                faceRecognitionProcessor.registerFace(employee, body, tempVector, RegisterFace.this, root);
            }
        });
        builder.show();
    }
}

