package com.example.trackingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class Followermainscreen extends AppCompatActivity {
    String phonenumber;
    FirebaseAuth auth;

    DatabaseReference reference;
    String currentuserphoneNumber;
    ListView listView;
    ArrayList<String> Followee;
    ArrayList<Double> Lat;
    ArrayList<Double> Lon;
    ArrayAdapter adapter = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followermainscreen);

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        listView = (ListView)findViewById(R.id.followeeList);
        Followee = new ArrayList<String>();
        Lat = new ArrayList<Double>();
        Lon = new ArrayList<Double>();
        adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,Followee);
        listView.setAdapter(adapter);

        findViewById(R.id.btnPeer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(Followermainscreen.this, UserLocationMainActivity.class);
                startActivity(myIntent);
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentuserphoneNumber = user.getPhoneNumber();

        reference.child(currentuserphoneNumber).child("Followee").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final User followee = dataSnapshot.getValue(User.class);
                Followee.add(followee.name);
                Lat.add(followee.lat);
                Lon.add(followee.lon);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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
                String currentfollowee = parent.getItemAtPosition(position).toString();

                Intent mapActivity  = new Intent(Followermainscreen.this,MapsActivity.class);
                mapActivity.putExtra("followee", currentfollowee);
                mapActivity.putExtra("lat", Lat.get(position));
                mapActivity.putExtra("lon", Lon.get(position));
                startActivity(mapActivity);
            }
        });



    }


}
