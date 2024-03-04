package com.example.checking.Model;

public class FaceModel {
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