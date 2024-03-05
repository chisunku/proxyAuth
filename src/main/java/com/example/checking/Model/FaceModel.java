package com.example.checking.Model;

import java.io.Serializable;

public class FaceModel implements Serializable {
    private String name;
    private float[] faceVector;

    public FaceModel(String name, float[] faceVector) {
        this.name = name;
        this.faceVector = faceVector;
    }

    public String getName() {
        return name;
    }

    public float[] getFaceVector() {
        return faceVector;
    }
}