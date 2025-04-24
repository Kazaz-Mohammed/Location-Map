package com.example.locationtracker;

public class LocationModel {
    private double latitude;
    private double longitude;
    private String timestamp;

    public LocationModel(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getTimestamp() { return timestamp; }
}
