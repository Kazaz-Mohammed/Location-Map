package com.example.locationtracker;

import java.util.List;

public class LocationResponse {
    private String status;
    private List<LocationModel> data;

    public String getStatus() {
        return status;
    }

    public List<LocationModel> getData() {
        return data;
    }
}
