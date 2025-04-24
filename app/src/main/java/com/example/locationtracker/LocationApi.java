package com.example.locationtracker;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
public interface LocationApi {
    @Headers("Content-Type: application/json")
    @POST("save_location.php")
    Call<Void> sendLocation(@Body LocationModel location);

    @GET("get_locations.php")
    Call<LocationResponse> getLocations();

}
