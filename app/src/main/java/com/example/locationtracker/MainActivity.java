package com.example.locationtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.Marker;


import android.view.GestureDetector;
import android.view.MotionEvent;
import android.content.Context;


import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


import android.location.Location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.util.GeoPoint;


public class MainActivity extends FragmentActivity {


    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map = null;

    private ToggleButton toggleTracking;

    private boolean isTracking = false;

    private boolean isFirstLocation = true;
    private Polyline savedPathLine;

    private boolean isSavedPathVisible = true;
    private Marker startMarker;
    private Marker stopMarker;


    private GestureDetector gestureDetector;



    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private ArrayList<GeoPoint> pathPoints = new ArrayList<>();
    private Polyline pathLine;




    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName()); // Needed for tiles

        setContentView(R.layout.activity_main);

        map = findViewById(R.id.map);
        map.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(18.0);

        toggleTracking = findViewById(R.id.btnToggleTracking);

        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

        MyLocationNewOverlay locationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(this), map);
        locationOverlay.enableMyLocation();
        map.getOverlays().add(locationOverlay);


        // Center map on current location when it's first available
        locationOverlay.runOnFirstFix(() -> {
            GeoPoint myLocation = locationOverlay.getMyLocation();
            if (myLocation != null) {
                runOnUiThread(() -> {
                    map.getController().setCenter(myLocation); // or animateTo
                });
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        pathLine = new Polyline(); // For the movement path
        pathLine.setWidth(5f);
        map.getOverlays().add(pathLine);


        toggleTracking.setOnCheckedChangeListener(null); // Remove any previous listener



        toggleTracking.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTracking = isChecked;
            if (isChecked) {
                Toast.makeText(this, "Tracking Started", Toast.LENGTH_SHORT).show();
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Tracking Stopped", Toast.LENGTH_SHORT).show();
                stopLocationUpdates();
            }
        });


        LocationApi api = RetrofitClient.getRetrofitInstance().create(LocationApi.class);

        api.getLocations().enqueue(new Callback<LocationResponse>() {
            @Override
            public void onResponse(Call<LocationResponse> call, Response<LocationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LocationModel> savedLocations = response.body().getData();

                    List<GeoPoint> points = new ArrayList<>();
                    for (LocationModel loc : savedLocations) {
                        points.add(new GeoPoint(loc.getLatitude(), loc.getLongitude()));
                    }

//                    Polyline savedPath = new Polyline();
//                    savedPath.setPoints(points);
//                    savedPath.setColor(Color.BLUE);
//                    savedPath.setWidth(5f);
//                    map.getOverlayManager().add(savedPath);

                    savedPathLine = new Polyline();
                    savedPathLine.setPoints(points);
                    savedPathLine.setColor(Color.BLUE);
                    savedPathLine.setWidth(5f);
                    map.getOverlays().add(savedPathLine);
                    isSavedPathVisible = true; // Ensure toggle starts synced


                    map.invalidate();
                } else {
                    Log.e("History", "Server error");
                }
            }

            @Override
            public void onFailure(Call<LocationResponse> call, Throwable t) {
                Log.e("History", "Failed to load saved path", t);
            }
        });


//        Button btnLoadPath = findViewById(R.id.btnLoadPath);
//        btnLoadPath.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                loadSavedPath();
//            }
//        });


//        Button btnToggleSavedPath = findViewById(R.id.btnToggleSavedPath);
//        btnToggleSavedPath.setOnClickListener(v -> {
////            if (isSavedPathVisible) {
////                map.getOverlays().remove(savedPathLine);
////                btnToggleSavedPath.setText("Show Saved Path");
////            } else {
////                map.getOverlays().add(savedPathLine);
////                btnToggleSavedPath.setText("Hide Saved Path");
////            }
////            map.invalidate(); // Redraw the map
////            isSavedPathVisible = !isSavedPathVisible;
//
//
//            if (savedPathLine == null) return; // safety check
//
//            if (isSavedPathVisible) {
//                map.getOverlays().remove(savedPathLine);
//                btnToggleSavedPath.setText("Show Saved Path");
//            } else {
//                map.getOverlays().add(savedPathLine);
//                btnToggleSavedPath.setText("Hide Saved Path");
//            }
//
//            isSavedPathVisible = !isSavedPathVisible;
//            map.invalidate(); // refresh the map
//        });


        // Add long press detection to the map
        map.setMapListener(new org.osmdroid.events.MapListener() {
            @Override
            public boolean onScroll(org.osmdroid.events.ScrollEvent event) {
                return false;
            }

            @Override
            public boolean onZoom(org.osmdroid.events.ZoomEvent event) {
                return false;
            }
        });

// Add a custom long press listener
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                // Convert screen touch point to map coordinates
                org.osmdroid.api.IGeoPoint mapPoint = map.getProjection().fromPixels((int)e.getX(), (int)e.getY());
                GeoPoint geoPoint = new GeoPoint(mapPoint.getLatitude(), mapPoint.getLongitude());

                // Add marker at this point
                addLocationMarker(geoPoint);
            }
        });

// Override the map's touch event
        map.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return false; // Return false to allow the map to handle other touch events normally
            }
        });

    }


    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(3000); // 3 seconds
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                for (Location location : locationResult.getLocations()) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();

                    GeoPoint point = new GeoPoint(lat, lon);
                    pathPoints.add(point);
                    pathLine.setPoints(pathPoints);
//                    map.getController().animateTo(point);

                    if (isFirstLocation) {
                        map.getController().setZoom(18.0);
                        map.getController().setCenter(point);
                        isFirstLocation = false;
                    }


                    // ✅ ADD START MARKER
//                    startMarker = new Marker(map);
//                    startMarker.setPosition(point);
//                    startMarker.setTitle("Start");
//                    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//                    map.getOverlays().add(startMarker);
//                    map.invalidate(); // refresh map


                    // Send location to server
                    LocationApi api = RetrofitClient.getRetrofitInstance().create(LocationApi.class);
                    LocationModel locationModel = new LocationModel(lat, lon);

                    api.sendLocation(locationModel).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Log.d("Retrofit", "Location sent successfully");
                            } else {
                                try {
                                    Log.e("Retrofit", "Error sending: " + response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e("Retrofit", "Network error", t);
                        }
                    });


                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }

    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        if (!pathPoints.isEmpty()) {
            GeoPoint lastPoint = pathPoints.get(pathPoints.size() - 1);

            // ✅ Add stop marker
//            stopMarker = new Marker(map);
//            stopMarker.setPosition(lastPoint);
//            stopMarker.setTitle("Stop");
//            stopMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//            map.getOverlays().add(stopMarker);
//
//            map.invalidate(); // Redraw
        }

    }



    private void requestPermissionsIfNecessary(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS_REQUEST_CODE);
                return;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

//    private void loadSavedPath() {
//        LocationApi api = RetrofitClient.getRetrofitInstance().create(LocationApi.class);
//
//        api.getLocations().enqueue(new Callback<LocationResponse>() {
//            @Override
//            public void onResponse(Call<LocationResponse> call, Response<LocationResponse> response) {
//
//                Log.d("History", "Raw response: " + response.toString());
//
//                if (response.body() == null) {
//                    try {
//                        Log.e("History", "Error body: " + response.errorBody().string());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//
//                if (response.isSuccessful() && response.body() != null) {
//                    List<LocationModel> savedLocations = response.body().getData();
//
//                    List<GeoPoint> points = new ArrayList<>();
//                    for (LocationModel loc : savedLocations) {
//                        points.add(new GeoPoint(loc.getLatitude(), loc.getLongitude()));
//                    }
//
//                    Polyline savedPath = new Polyline();
//                    savedPath.setPoints(points);
//                    savedPath.setColor(Color.BLUE);
//                    savedPath.setWidth(5f);
//                    map.getOverlays().add(savedPath);
//                    map.invalidate();
//                } else {
//                    Log.e("History", "Server error");
//                }
//            }
//
//            @Override
//            public void onFailure(Call<LocationResponse> call, Throwable t) {
//                Log.e("History", "Failed to load saved path", t);
//            }
//        });
//    }


//    private void addLocationMarker(GeoPoint point) {
//        // Remove previous long-press marker if exists
//        for (int i = map.getOverlays().size() - 1; i >= 0; i--) {
//            if (map.getOverlays().get(i) instanceof Marker) {
//                Marker m = (Marker) map.getOverlays().get(i);
//                if (m.getId() != null && m.getId().equals("long_press_marker")) {
//                    map.getOverlays().remove(i);
//                    break;
//                }
//            }
//        }
//
//        // Create a new marker
//        Marker marker = new Marker(map);
//        marker.setPosition(point);
//        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//        marker.setId("long_press_marker");
//
//        // Format location for display
//        String locationText = String.format("Location: %.5f, %.5f",
//                point.getLatitude(),
//                point.getLongitude());
//        marker.setTitle(locationText);
//
//        // Add marker to map
//        map.getOverlays().add(marker);
//
//        // Show info window (optional)
//        marker.showInfoWindow();
//
//        // Refresh the map
//        map.invalidate();
//
//        // Provide feedback to user
//        Toast.makeText(MainActivity.this,
//                "Marker placed at " + locationText,
//                Toast.LENGTH_SHORT).show();
//    }


    private void addLocationMarker(GeoPoint point) {
        // Remove previous long-press marker if exists
        for (int i = map.getOverlays().size() - 1; i >= 0; i--) {
            if (map.getOverlays().get(i) instanceof Marker) {
                Marker m = (Marker) map.getOverlays().get(i);
                if (m.getId() != null && m.getId().equals("long_press_marker")) {
                    map.getOverlays().remove(i);
                    break;
                }
            }
        }

        // Create a new marker
        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setId("long_press_marker");

        // Format location for display
        String locationText = String.format("Location: %.5f, %.5f",
                point.getLatitude(),
                point.getLongitude());
        marker.setTitle(locationText);

        // Add marker to map
        map.getOverlays().add(marker);

        // Show info window
        marker.showInfoWindow();

        // Refresh the map
        map.invalidate();

        // Save the location to Supabase
        saveLocationToSupabase(point.getLatitude(), point.getLongitude());

        // Provide feedback to user
        Toast.makeText(MainActivity.this,
                "Marker placed and saved to database",
                Toast.LENGTH_SHORT).show();
    }

    private void saveLocationToSupabase(double latitude, double longitude) {
        LocationApi api = RetrofitClient.getRetrofitInstance().create(LocationApi.class);
        LocationModel locationModel = new LocationModel(latitude, longitude);

        api.sendLocation(locationModel).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("Supabase", "Marker location saved successfully");
                } else {
                    try {
                        Log.e("Supabase", "Error saving marker: " + response.errorBody().string());
                        Toast.makeText(MainActivity.this,
                                "Failed to save marker to database",
                                Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Supabase", "Network error saving marker", t);
                Toast.makeText(MainActivity.this,
                        "Network error: Could not save marker",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}