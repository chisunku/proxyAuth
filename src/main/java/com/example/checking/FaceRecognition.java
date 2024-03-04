package com.example.checking;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.checking.Model.Attendance;
import com.example.checking.Model.Employee;
import com.example.checking.Model.FaceModel;
import com.example.checking.R;
import com.example.checking.Service.APIService;
import com.example.checking.Service.RetrofitClient;
import com.example.checking.helpers.MLVideoHelperActivity;
import com.example.checking.helpers.vision.VisionBaseProcessor;
import com.example.checking.helpers.vision.recogniser.FaceRecognitionProcessor;
import com.google.mlkit.vision.face.Face;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FaceRecognition extends MLVideoHelperActivity implements FaceRecognitionProcessor.FaceRecognitionCallback {

    private Interpreter faceNetInterpreter;
    private FaceRecognitionProcessor faceRecognitionProcessor;

    private Face face;
    private Bitmap faceBitmap;
    private float[] faceVector;

    public static FaceModel empFace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        makeAddFaceVisible();

    }

    //setting the pretrained tenserflow model
    @Override
    protected VisionBaseProcessor setProcessor() {
        try {
            faceNetInterpreter = new Interpreter(FileUtil.loadMappedFile(this, "mobile_face_net.tflite"), new Interpreter.Options());
        } catch (IOException e) {
            e.printStackTrace();
        }

        faceRecognitionProcessor = new FaceRecognitionProcessor(
                faceNetInterpreter,
                graphicOverlay,
                this
        );
        faceRecognitionProcessor.activity = this;
        return faceRecognitionProcessor;
    }

    public void setTestImage(Bitmap cropToBBox) {
        if (cropToBBox == null) {
            return;
        }
        runOnUiThread(() -> ((ImageView) findViewById(R.id.testImageView)).setImageBitmap(cropToBBox));
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
        Log.d("onFaceRecignised", "face recognized : probability : "+probability+" name : "+name);
        Toast.makeText(this, "Face Found!!", Toast.LENGTH_SHORT).show();
        Intent resultIntent = new Intent();
        resultIntent.putExtra("Face", "OK!"); // You can put data to be passed back
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        //face has been recognized not do the checkin process
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Editable input  = ((EditText) dialogView.findViewById(R.id.dlg_input)).getEditableText();
                if (input.length() > 0) {
                    Log.d("register the user in facerec", "coming into the if");
                    faceRecognitionProcessor.registerFace(input, tempVector);
                }
            }
        });
        builder.show();
    }
}
