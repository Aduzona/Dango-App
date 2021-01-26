package com.example.trackingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Color;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FollowerScreenActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String phonenumber;
    FirebaseAuth auth;

    DatabaseReference reference;
    String currentuserphoneNumber;
    ListView listView;
    //ArrayList<String> Followee;
    ArrayList<Double> Lat;
    ArrayList<Double> Lon;
    ArrayList<String> Name;

    //ArrayList<ArrayList> Foloweeroute;
    ArrayList<com.google.android.gms.maps.model.LatLng> Points;
    com.example.trackingapp.LatLng point;
    Map<String, LatLng> Followee;
    Map<String, LatLng> StartLocation;
    Map<String, LatLng> EndLocation;
    Map<String, ArrayList> Foloweeroute;

    Marker currentfolloweemarker;
    ArrayAdapter adapter = null;
    Route route;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower_screen);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        listView = (ListView)findViewById(R.id.followeeList);
        Followee = new HashMap<>();
        Points = new ArrayList<>();
        StartLocation = new  HashMap<>();
        EndLocation = new HashMap<>();
        Name = new ArrayList<>();
        Foloweeroute =  new HashMap<>();
        adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,Name);
        listView.setAdapter(adapter);
        findViewById(R.id.btnPeer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(FollowerScreenActivity.this, UserLocationMainActivity.class);
                startActivity(myIntent);
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentuserphoneNumber = user.getPhoneNumber();

        reference.child(currentuserphoneNumber).child("Followee").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Points = new ArrayList<>();
                //Curent location
                try{
                    User followee = dataSnapshot.child("Current location").getValue(User.class);
                    if(followee.name != null) {
                        Followee.put(followee.name, new com.google.android.gms.maps.model.LatLng(followee.lat, followee.lon));

                        //Polylines
                        for (DataSnapshot postSnapshot : dataSnapshot.child("Route").child("points").getChildren()) {
                            com.example.trackingapp.LatLng point = postSnapshot.getValue(com.example.trackingapp.LatLng.class);
                            com.google.android.gms.maps.model.LatLng pointnew = new com.google.android.gms.maps.model.LatLng(point.getLatitude(), point.getLongitude());
                            Points.add(pointnew);
                        }
                        Foloweeroute.put(followee.name, Points);

                        //StartLocation & Endlocation
                        com.example.trackingapp.LatLng startlocation = dataSnapshot.child("Route").child("startLocation").getValue(com.example.trackingapp.LatLng.class);
                        com.google.android.gms.maps.model.LatLng startlocationnew = new com.google.android.gms.maps.model.LatLng(startlocation.getLatitude(), startlocation.getLongitude());
                        StartLocation.put(followee.name, startlocationnew);
                        Log.i("StartLocation", StartLocation.toString());
                        com.example.trackingapp.LatLng endLocation = dataSnapshot.child("Route").child("endLocation").getValue(com.example.trackingapp.LatLng.class);
                        com.google.android.gms.maps.model.LatLng endLocationnew = new com.google.android.gms.maps.model.LatLng(endLocation.getLatitude(), endLocation.getLongitude());
                        EndLocation.put(followee.name, endLocationnew);
                        Log.i("EndLocation", EndLocation.toString());


                        //Routelist.add(route);
                        Name.add(followee.name);
                        adapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Points = new ArrayList<>();
                //Curent location

                try {
                    User followee = dataSnapshot.child("Current location").getValue(User.class);
                    if(followee.name != null) {
                        Followee.put(followee.name, new com.google.android.gms.maps.model.LatLng(followee.lat, followee.lon));

                        //Polylines
                        for (DataSnapshot postSnapshot : dataSnapshot.child("Route").child("points").getChildren()) {
                            com.example.trackingapp.LatLng point = postSnapshot.getValue(com.example.trackingapp.LatLng.class);
                            com.google.android.gms.maps.model.LatLng pointnew = new com.google.android.gms.maps.model.LatLng(point.getLatitude(), point.getLongitude());
                            Points.add(pointnew);
                        }
                        Foloweeroute.put(followee.name, Points);
                        //StartLocation & Endlocation
                        com.example.trackingapp.LatLng startlocation = dataSnapshot.child("Route").child("startLocation").getValue(com.example.trackingapp.LatLng.class);
                        com.google.android.gms.maps.model.LatLng startlocationnew = new com.google.android.gms.maps.model.LatLng(startlocation.getLatitude(), startlocation.getLongitude());
                        StartLocation.put(followee.name, startlocationnew);
                        Log.i("StartLocation", StartLocation.toString());
                        com.example.trackingapp.LatLng endLocation = dataSnapshot.child("Route").child("endLocation").getValue(com.example.trackingapp.LatLng.class);
                        com.google.android.gms.maps.model.LatLng endLocationnew = new com.google.android.gms.maps.model.LatLng(endLocation.getLatitude(), endLocation.getLongitude());
                        EndLocation.put(followee.name, endLocationnew);
                        Log.i("EndLocation", EndLocation.toString());


                        //Routelist.add(route);
                        if(Name.contains(followee.name)){}else{
                            Name.add(followee.name);
                            adapter.notifyDataSetChanged();}
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mMap.clear();
                final String currentfollowee = parent.getItemAtPosition(position).toString();
                PolylineOptions polylineOptions = new PolylineOptions().
                        geodesic(true).
                        color(Color.BLUE).
                        width(5);
                mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue)).position(StartLocation.get(currentfollowee)));
                mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green)).position(EndLocation.get(currentfollowee)));
                for (int i = 0; i < Foloweeroute.get(currentfollowee).size(); i++) {
                    polylineOptions.add((LatLng) Foloweeroute.get(currentfollowee).get(i));
                }
                mMap.addPolyline(polylineOptions);
                currentfolloweemarker = mMap.addMarker(new MarkerOptions().position(Followee.get(currentfollowee)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Followee.get(currentfollowee), 16));


                reference.child(currentuserphoneNumber).child("Followee").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        User followee = dataSnapshot.child("Current location").getValue(User.class);
                        Followee.put(followee.name,new com.google.android.gms.maps.model.LatLng(followee.lat,followee.lon));
                        //currentfolloweemarker = mMap.addMarker(new MarkerOptions().position(Followee.get(currentfollowee)));
                        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Followee.get(currentfollowee), 16));
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (currentfolloweemarker!=null){
                            currentfolloweemarker.remove();
                        }
                        User followee = dataSnapshot.child("Current location").getValue(User.class);
                        Followee.put(followee.name,new com.google.android.gms.maps.model.LatLng(followee.lat,followee.lon));
                        currentfolloweemarker = mMap.addMarker(new MarkerOptions().position(Followee.get(currentfollowee)));
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
    }
}
