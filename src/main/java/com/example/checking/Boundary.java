package com.example.checking;

public class Boundary {
    private String name;
    private double latitude;
    private double longitude;
    private float radius;

    public Boundary() {
        // Default constructor required for Firebase
    }

    public Boundary(String name, double latitude, double longitude, float radius) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    // Add getters and setters as needed
}
