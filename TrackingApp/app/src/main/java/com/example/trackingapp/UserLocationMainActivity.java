package com.example.trackingapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;


public class UserLocationMainActivity extends FragmentActivity implements OnMapReadyCallback,DirectionFinderListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private EditText etOrigin;
    private EditText etDestination;
    public String requiredtime;
    Integer z;
    Marker Destination,Origin;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    public Route chosenroute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getpermission();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_location_main);
        etOrigin = (EditText) findViewById(R.id.etOrigin);
        etDestination = (EditText) findViewById(R.id.etDestination);
        requiredtime = "0";
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if(haveNetworkConnection()){
        }else{
            Toast.makeText(UserLocationMainActivity.this,"Please enable your Internet connection",Toast.LENGTH_SHORT).show();
        }



        findViewById(R.id.car).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest("driving");
            }
        });

        findViewById(R.id.walk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest("walking");
            }
        });

        findViewById(R.id.bike).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest("bicycling ");
            }
        });
        findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chosenroute != null){
                    Intent myIntent = new Intent(UserLocationMainActivity.this, CheckpointActivity.class);
                    myIntent.putExtra("route",chosenroute);
                    startActivity(myIntent);}
                else{
                    Toast.makeText(UserLocationMainActivity.this, "Please choose a route", Toast.LENGTH_SHORT).show();};

            }
        });
        findViewById(R.id.btnFollower).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(UserLocationMainActivity.this, FollowerScreenActivity.class);
                startActivity(myIntent);
            }
        });

        findViewById(R.id.currentlocationbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getpermission();
                if(haveNetworkConnection()){
                    try{
                        getCurrentLocation();} catch (Exception e) {
                        e.printStackTrace();
                    }

                }else{
                    Toast.makeText(UserLocationMainActivity.this,"Please enable your Internet connection",Toast.LENGTH_SHORT).show();
                }



            }
        });
        findViewById(R.id.Resetbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etOrigin.getText().clear();
                etDestination.getText().clear();
                ((TextView) findViewById(R.id.tvDuration)).setText("") ;
                ((TextView) findViewById(R.id.tvDistance)).setText("") ;
                mMap.clear();
            }
        });


    }


    public boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    private void getCurrentLocation(){
        final Geocoder geocoder = new Geocoder(UserLocationMainActivity.this, Locale.getDefault());
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.getFusedLocationProviderClient(UserLocationMainActivity.this)
                .requestLocationUpdates(locationRequest,new LocationCallback() {

                    @Override
                    public void onLocationResult(LocationResult locationResult){
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(UserLocationMainActivity.this)
                                .removeLocationUpdates(this);
                        if(locationResult!=null && locationResult.getLocations().size()>0){
                            int latestLocationIndex = locationResult.getLocations().size()-1;
                            double lat = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                            double lon = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                            try{
                                mMap.clear();
                                List<Address> addresses = geocoder.getFromLocation(lat,lon,1);
                                String address = addresses.get(0).getAddressLine(0);
                                etOrigin.setText(address);

                                LatLng currentloc = new LatLng(lat, lon);
                                Origin = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue)).position(currentloc).draggable(true).title("Current location"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentloc, 16));
//                                mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
//
//                                    @Override
//                                    public void onMarkerDrag(Marker arg0) {
//                                        // TODO Auto-generated method stub
//                                        Log.d("Marker", "Dragging");
//                                    }
//
//                                    @Override
//                                    public void onMarkerDragEnd(Marker arg0) {
//                                        // TODO Auto-generated method stub
//                                        LatLng markerLocation = origin.getPosition();
//                                        List<Address> addresses = null;
//                                        try {
//                                            addresses = geocoder.getFromLocation(markerLocation.latitude,markerLocation.longitude,1);
//                                        } catch (IOException e) {
//                                            e.printStackTrace();
//                                        }
//                                        String address = addresses.get(0).getAddressLine(0);
//                                        etOrigin.setText(address);
//
//                                    }
//
//                                    @Override
//                                    public void onMarkerDragStart(Marker arg0) {
//                                        // TODO Auto-generated method stub
//                                        Log.d("Marker", "Started");
//
//                                    }
//                                });


                            } catch (IOException e) {
                                e.printStackTrace();
                            }



                        }
                    }

                }, Looper.getMainLooper());
    }

    private void sendRequest(String mode) {
        String origin = etOrigin.getText().toString();
        String destination = etDestination.getText().toString();

        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(this, origin, destination,mode).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        z=5;
        final Geocoder geocoder = new Geocoder(UserLocationMainActivity.this, Locale.getDefault());
        mMap = googleMap;
        mMap.setMaxZoomPreference(16);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                polyline.setZIndex(z);
                polyline.setWidth(10);
                Route route = (Route) polyline.getTag();
                requiredtime = route.duration.text;
                ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
                ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);
                z=z+1;
                Log.i("fddgf",z.toString());
                chosenroute = route;
            }

        }
                );
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                try{
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
                    String address = addresses.get(0).getAddressLine(0);
                    etDestination.setText(address);
                    LatLng currentloc = new LatLng(latLng.latitude, latLng.longitude);
                    if (Destination!=null){
                        Destination.remove();
                    }
                    Destination = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green)).position(currentloc).draggable(true).title("Destination"));

                    etDestination.setText(address);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDrag(Marker arg0) {
                // TODO Auto-generated method stub
                Log.d("Marker", "Dragging");
            }

            @Override
            public void onMarkerDragEnd(Marker arg0) {
                // TODO Auto-generated method stub
                LatLng markerLocation = arg0.getPosition();
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(markerLocation.latitude,markerLocation.longitude,1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String address = addresses.get(0).getAddressLine(0);
                String name = arg0.getTitle();
                Log.i("asdf",name);
                if (name.contains("location")){
                    etOrigin.setText(address);

                }else{
                    etDestination.setText(address);
                }

            }

            @Override
            public void onMarkerDragStart(Marker arg0) {
                // TODO Auto-generated method stub
                Log.d("Marker", "Started");

            }
        });


    }

    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }
    public void onDirectionFinderSuccess(List<Route> routes) {
        mMap.clear();
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();
        Random random = new Random();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
//            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
//            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

            Origin = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue)).position(route.startLocation).draggable(true).title(route.startAddress));
            Destination = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green)).position(route.endLocation).draggable(true).title(route.endAddress));

//            originMarkers.add(mMap.addMarker(new MarkerOptions()
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
//                    .title(route.startAddress)
//                    .position(route.startLocation)));
//            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
//                    .title(route.endAddress)
//                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255))).
                    width(5);
            polylineOptions.zIndex(1);
            for (int i = 0; i < route.points.size(); i++) {
                polylineOptions.add(route.points.get(i));
            }
            Polyline polyline = mMap.addPolyline(polylineOptions);
            polyline.setTag(route);
            polyline.setClickable(true);

            polylinePaths.add(polyline);

        }

    }
    public void getpermission(){
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
        }else{ ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1);
        }

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
        }else{ ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                22);
        }


        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();

        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
// Retrieve the data from the marker.
        Integer clickCount = (Integer) marker.getTag();

        // Check if a click count was set, then display the click count.
//        if (clickCount != null) {
//            clickCount = clickCount + 1;
//            marker.setTag(clickCount);
//            Toast.makeText(this,
//                    marker.getTitle() +
//                            " has been clicked " + clickCount + " times.",
//                    Toast.LENGTH_SHORT).show();
//        }

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }
}
