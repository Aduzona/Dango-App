package com.example.trackingapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    // Write a message to the database
    FirebaseAuth auth;
    FirebaseUser user;
    String userId;
    Button signup, signin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth =FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        signup=(Button) findViewById(R.id.Signup);

        // check if user is logged in
        if (user == null) {
            setContentView(R.layout.activity_main);
            signup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToRegister(v);
                }
            });

        }else{
            getpermission();
            Intent myIntent = new Intent(MainActivity.this, UserLocationMainActivity.class);
            startActivity(myIntent);
            finish();
        }
    }

//    public void goToLogin(View v){
//        Intent myIntent = new Intent(MainActivity.this,LoginActivity.class);
//        startActivity(myIntent);
//        finish();
//    }

    public void goToRegister(View v){
        Intent myIntent = new Intent(MainActivity.this,registerbyphone.class);
        startActivity(myIntent);
        finish();
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


}
