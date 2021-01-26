package com.example.trackingapp;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Movie;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CheckpointActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Route route;
    Marker Destination,Origin;
    ListView listView;
    ArrayList<String> Checkpoints;
    ArrayAdapter adapter = null;
    ArrayList<Object> Checkpointsobject;
    Map<String,Checkpoint> Checkpointsmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkpoint);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        listView = (ListView)findViewById(R.id.Checkpointslist);
        Checkpoints = new ArrayList<String>();
        adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,Checkpoints);
        listView.setAdapter(adapter);
        Checkpointsobject = new ArrayList<Object>();


        Checkpointsmap = new HashMap<>(); // this map includes (Address,Latlng)

        route = (Route) getIntent().getParcelableExtra("route"); //receibe

        findViewById(R.id.btnNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(CheckpointActivity.this, JourneyInfo.class);
                    myIntent.putExtra("route",route);

                    Bundle extras = new Bundle();
                    extras.putSerializable("Checkpointsmap", (Serializable) Checkpointsmap);
                    myIntent.putExtras(extras);
                    startActivity(myIntent);

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final Geocoder geocoder = new Geocoder(CheckpointActivity.this, Locale.getDefault());

        // Add a marker in Sydney and move the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
        Origin = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue)).position(route.startLocation).title("Departure"));
        Origin.setTag("1");

        Destination = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green)).position(route.endLocation).title("Destination"));
        Destination.setTag("1");
        PolylineOptions polylineOptions = new PolylineOptions().
                geodesic(true).
                color(Color.BLUE).
                width(5);
        polylineOptions.zIndex(1);
        for (int i = 0; i < route.points.size(); i++) {
            polylineOptions.add(route.points.get(i));
        }
        mMap.addPolyline(polylineOptions);


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (!marker.getTitle().contains("Departure") && !marker.getTitle().contains("Destination")){
                    Checkpoints.remove(marker.getTitle());
                    marker.remove();
                    adapter.notifyDataSetChanged();
//                    Checkpointsobject.remove(new Checkpoint(marker.getTitle(),marker.getPosition()));
                    Checkpointsmap.remove(marker.getTitle());
                    Log.i("fdgdsg",Checkpointsmap.toString());
                }

                return true;
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                try{
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
                    String address = addresses.get(0).getAddressLine(0);
                    LatLng currentloc = new LatLng(latLng.latitude, latLng.longitude);

                    Checkpoint z = new Checkpoint(address,latLng);
                    Checkpointsmap.put(address,z); // Or use UUID directly as key

                    Checkpoints.add(address);
                    mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.location)).position(currentloc).draggable(true).title(address));
                    //Checkpointsobject.add(z);
                    Log.i("fdgdsg",Checkpointsmap.toString());

                    adapter.notifyDataSetChanged();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
