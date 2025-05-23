package com.example.locationtracker;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
public interface LocationApi {
//    @Headers("Content-Type: application/json")
//    @POST("save_location.php")
//    Call<Void> sendLocation(@Body LocationModel location);
//
//    @GET("get_locations.php")
//    Call<LocationResponse> getLocations();


    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9jdnp4dmthd2xoamxocHdycXdoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDU1MzEyNjIsImV4cCI6MjA2MTEwNzI2Mn0.Ju9HIi07RfctFO14r2LU0m6I0OvLAKG5Q0fGisYuQ7M",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9jdnp4dmthd2xoamxocHdycXdoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDU1MzEyNjIsImV4cCI6MjA2MTEwNzI2Mn0.Ju9HIi07RfctFO14r2LU0m6I0OvLAKG5Q0fGisYuQ7M",
            "Content-Type: application/json"
    })
    @POST("rest/v1/locations")
    Call<Void> sendLocation(@Body LocationModel location);

    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9jdnp4dmthd2xoamxocHdycXdoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDU1MzEyNjIsImV4cCI6MjA2MTEwNzI2Mn0.Ju9HIi07RfctFO14r2LU0m6I0OvLAKG5Q0fGisYuQ7M",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9jdnp4dmthd2xoamxocHdycXdoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDU1MzEyNjIsImV4cCI6MjA2MTEwNzI2Mn0.Ju9HIi07RfctFO14r2LU0m6I0OvLAKG5Q0fGisYuQ7M"
    })
    @GET("rest/v1/locations?select=*")
    Call<LocationResponse> getLocations();
}
