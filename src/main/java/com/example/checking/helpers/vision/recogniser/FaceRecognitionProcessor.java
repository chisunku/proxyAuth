package com.example.checking.helpers.vision.recogniser;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.text.Editable;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageProxy;

import com.example.checking.Model.Employee;
import com.example.checking.Model.FaceModel;
import com.example.checking.Service.APIService;
import com.example.checking.Service.RetrofitClient;
import com.example.checking.helpers.vision.FaceGraphic;
import com.example.checking.helpers.vision.GraphicOverlay;
import com.example.checking.helpers.vision.VisionBaseProcessor;
import com.example.checking.FaceRecognition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FaceRecognitionProcessor extends VisionBaseProcessor<List<Face>> {

    APIService apiService = RetrofitClient.getClient().create(APIService.class);

    public interface FaceRecognitionCallback {
        void onFaceRecognised(Face face, float probability, String name);
        void onFaceDetected(Face face, Bitmap faceBitmap, float[] vector);
    }

    private static final String TAG = "FaceRecognitionProcessor";

    // Input image size for our facenet model
    private static final int FACENET_INPUT_IMAGE_SIZE = 112;

    private final FaceDetector detector;
    private final Interpreter faceNetModelInterpreter;
    private final ImageProcessor faceNetImageProcessor;
    private final GraphicOverlay graphicOverlay;
    private final FaceRecognitionCallback callback;

    public FaceRecognition activity;

    static FaceModel empFace;

    List<FaceModel> recognisedFaceList = new ArrayList();

    Employee employee;

    public FaceRecognitionProcessor(Interpreter faceNetModelInterpreter,
                                    GraphicOverlay graphicOverlay,
                                    FaceRecognitionCallback callback,
                                    Employee employee,
                                    FaceModel empFace) {
        this.callback = callback;
        this.graphicOverlay = graphicOverlay;
        this.employee = employee;
        this.empFace = empFace;
        // initialize processors
        this.faceNetModelInterpreter = faceNetModelInterpreter;
        faceNetImageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(FACENET_INPUT_IMAGE_SIZE, FACENET_INPUT_IMAGE_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                .add(new NormalizeOp(0f, 255f))
                .build();

        FaceDetectorOptions faceDetectorOptions = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                // to ensure we don't count and analyse same person again
                .enableTracking()
                .build();
        detector = FaceDetection.getClient(faceDetectorOptions);
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    public Task<List<Face>> detectInImage(ImageProxy imageProxy, Bitmap bitmap, int rotationDegrees) {
        InputImage inputImage = InputImage.fromMediaImage(imageProxy.getImage(), rotationDegrees);
        int rotation = rotationDegrees;

        // In order to correctly display the face bounds, the orientation of the analyzed
        // image and that of the viewfinder have to match. Which is why the dimensions of
        // the analyzed image are reversed if its rotation information is 90 or 270.
        boolean reverseDimens = rotation == 90 || rotation == 270;
        int width;
        int height;
        if (reverseDimens) {
            width = imageProxy.getHeight();
            height =  imageProxy.getWidth();
        } else {
            width = imageProxy.getWidth();
            height = imageProxy.getHeight();
        }
        return detector.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                    @Override
                    public void onSuccess(List<Face> faces) {
                        graphicOverlay.clear();
                        for (Face face : faces) {
                            FaceGraphic faceGraphic = new FaceGraphic(graphicOverlay, face, false, width, height);
                            Log.d(TAG, "face found, id: " + face.getTrackingId());
//                            if (activity != null) {
//                                activity.setTestImage(cropToBBox(bitmap, face.getBoundingBox(), rotation));
//                            }
                            // now we have a face, so we can use that to analyse age and gender
                            Bitmap faceBitmap = cropToBBox(bitmap, face.getBoundingBox(), rotation);

                            if (faceBitmap == null) {
                                Log.d("GraphicOverlay", "Face bitmap null");
                                return;
                            }

                            TensorImage tensorImage = TensorImage.fromBitmap(faceBitmap);
                            ByteBuffer faceNetByteBuffer = faceNetImageProcessor.process(tensorImage).getBuffer();
                            float[][] faceOutputArray = new float[1][192];
                            faceNetModelInterpreter.run(faceNetByteBuffer, faceOutputArray);

                            Log.d(TAG, "output array: " + Arrays.deepToString(faceOutputArray));

                            if (callback != null) {
                                callback.onFaceDetected(face, faceBitmap, faceOutputArray[0]);
                                Log.d("face list", "recognisedFaceList : "+recognisedFaceList);
//                                if (!recognisedFaceList.isEmpty()) {
                                    Pair<String, Float> result = findNearestFace(faceOutputArray[0]);
                                    // if distance is within confidence
                                    if (result!=null && result.second < 1.0f) {
                                        faceGraphic.name = result.first;
                                        callback.onFaceRecognised(face, result.second, result.first);
                                        Log.d(TAG, "Face found!");
                                    }
//                                }
                            }
                            graphicOverlay.add(faceGraphic);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // intentionally left empty
                    }
                });
    }

    // looks for the nearest vector in the dataset (using L2 norm)
    // and returns the pair <name, distance>
    private Pair<String, Float> findNearestFace(float[] vector) {
        Log.d("in face rec processor", "finding nearest face");
        //need to change this
        final Pair<String, Float>[] ret = new Pair[]{null};
        if(empFace == null) {
            APIService apiService = RetrofitClient.getClient().create(APIService.class);
            Call<Employee> call = apiService.getEmployeeByEmail(employee.getEmail());
            call.enqueue(new Callback<Employee>() {
                @Override
                public void onResponse(Call<Employee> call, Response<Employee> response) {
                    System.out.println("response: " + response);
                    if (response.isSuccessful()) {
                        empFace = response.body().getFace();
                        Log.d("faceRec API success", "success face got : " + empFace.getName());
                        final String name = empFace.getName();
                        final float[] knownVector = empFace.getFaceVector();

                        float distance = 0;
                        for (int i = 0; i < vector.length; i++) {
                            float diff = vector[i] - knownVector[i];
                            distance += diff * diff;
                        }
                        distance = (float) Math.sqrt(distance);
                        if (ret[0] == null || distance < ret[0].second) {
                            ret[0] = new Pair<>(name, distance);
                        }
                    } else {
                        Log.d("faceRec API call", "API call to get emp face has no response");
                    }
                }

                @Override
                public void onFailure(Call<Employee> call, Throwable t) {
                    // Handle network errors
                    Log.d("Face rec ", "Get face API error: " + t.fillInStackTrace());
                }
            });
        }
        else{
            final String name = empFace.getName();
            Log.d(TAG,"in else and name is "+name);
            final float[] knownVector = empFace.getFaceVector();

            float distance = 0;
            for (int i = 0; i < vector.length; i++) {
                float diff = vector[i] - knownVector[i];
                distance += diff * diff;
            }
            distance = (float) Math.sqrt(distance);
            if (ret[0] == null || distance < ret[0].second) {
                ret[0] = new Pair<>(name, distance);
            }
        }
//        for (FaceModel person : recognisedFaceList) {
//        if(empFace!=null) {
//            final String name = empFace.getName();
//            final float[] knownVector = empFace.getFaceVector();
//
//            float distance = 0;
//            for (int i = 0; i < vector.length; i++) {
//                float diff = vector[i] - knownVector[i];
//                distance += diff * diff;
//            }
//            distance = (float) Math.sqrt(distance);
//            if (ret == null || distance < ret.second) {
//                ret = new Pair<>(name, distance);
//            }
//        }
//        }

        Log.d("in fae rec processor", "returned results : "+ ret[0]);

        return ret[0];

    }

    public void stop() {
        detector.close();
    }

    private Bitmap cropToBBox(Bitmap image, Rect boundingBox, int rotation) {
        int shift = 0;
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
        }
        if (boundingBox.top >= 0 && boundingBox.bottom <= image.getWidth()
                && boundingBox.top + boundingBox.height() <= image.getHeight()
                && boundingBox.left >= 0
                && boundingBox.left + boundingBox.width() <= image.getWidth()) {
            return Bitmap.createBitmap(
                    image,
                    boundingBox.left,
                    boundingBox.top + shift,
                    boundingBox.width(),
                    boundingBox.height()
            );
        } else return null;
    }

    // Register a name against the vector
    public void registerFace(Editable input, float[] tempVector) {
        FaceModel face = new FaceModel(input.toString(), tempVector);
        recognisedFaceList.add(face);
        Employee emp = new Employee();
        emp.setName(input.toString());
        emp.setFace(face);
        emp.setEmail(input.toString()+"@proxyAuth.com");
        Log.d("face rec processor", "registerFace: "+emp.getEmail());
        //change this logic to admin later
        Call<Employee> call = apiService.registerEmp(emp);
        call.enqueue(new Callback<Employee>() {
            @Override
            public void onResponse(Call<Employee> call, Response<Employee> response) {
                Log.d("response in post api call employee", "response : "+response);
                if (response.isSuccessful()) {
                    // Handle successful response
//                    Toast.makeText(getC(), "Location added successfully", Toast.LENGTH_SHORT).show();
                    Log.d("register user", "registered successfully");
                } else {
                    // Handle unsuccessful response
//                    Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
                    Log.d("register user", "registration unsuccessful");
                }
            }

            @Override
            public void onFailure(Call<Employee> call, Throwable t) {
                // Handle network errors
                System.out.println("error: "+t.getStackTrace());
                Log.d("retrofit post reg emp call", "error"+t.getStackTrace());
            }
        });

    }
}
