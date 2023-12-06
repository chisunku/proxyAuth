package com.example.checking;

import java.io.Serializable;
import java.util.List;

public class LocationsModel implements Serializable {
    private String address;
    private String name;
    private List<Point> polygon;

    // Required default constructor for Firestore
    public LocationsModel() {
    }

    public LocationsModel(String address, String name, List<Point> polygon) {
        this.address = address;
        this.name = name;
        this.polygon = polygon;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Point> getPolygon() {
        return polygon;
    }

    public void setPolygon(List<Point> polygon) {
        this.polygon = polygon;
    }

    public static class Point {
        private double latitude;
        private double longitude;

        public Point() {
            // Required default constructor for Firestore
        }

        public Point(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }
}
