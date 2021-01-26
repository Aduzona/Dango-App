package com.example.trackingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.location.LocationListener;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class CountdownActivity extends AppCompatActivity {
    TextView mTextField;
    long min ;
    String time,currentusername,token;
    long extratime;
    Button mButtonStartPause;
    boolean mTimerRunning;
    private CountDownTimer mCountDownTimer;
    long mTimeLeftinMillis;
    String phonenumber;
    FirebaseAuth auth;
    private RequestQueue mRequestQue;
    private String URL = "https://fcm.googleapis.com/fcm/send";
    Route route;
    private LatLng latLng;
    DatabaseReference reference;
    Intent serviceIntent;
    String currentuserphoneNumber;
    HashMap<String, Checkpoint> Checkpointsmap;
    Integer checkpointnumber;
    private FusedLocationProviderClient mFusedLocationClient;
    double distance;
    private BroadcastReceiver mMessageReceiver;
    Context mcontext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {


                findViewById(R.id.arrivehome).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //User followee = new User(currentusername+" arrived home safely",0,0);
                        try{
                            reference.child(phonenumber).child("Followee").child(currentuserphoneNumber).child("Current location").child("name").setValue(currentusername);
                            sendNotification(currentusername+" is home","Thank you. I'm home!");
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver);
                        //stopservice
                        stopService(new Intent(serviceIntent));

                        mCountDownTimer.cancel();
                        Intent myIntent = new Intent(CountdownActivity.this, UserLocationMainActivity.class);
                        startActivity(myIntent);
                        finish();
                    }
                });


                findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //User followee = new User(currentusername+" canceled the journey",0,0);
                        try{
                            reference.child(phonenumber).child("Followee").child(currentuserphoneNumber).child("Current location").child("name").setValue(currentusername);
                            sendNotification(currentusername+" canceled the journey","Thank you. I canceled the journey!");
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }

                        LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver);
                        //stopservice
                        stopService(new Intent(serviceIntent));

                        mCountDownTimer.cancel();
                        Intent myIntent = new Intent(CountdownActivity.this, UserLocationMainActivity.class);
                        startActivity(myIntent);
                        finish();
                    }
                });

                mcontext = context;
                latLng =intent.getParcelableExtra("location"); //receibe
                Log.i("latlongcountdown",latLng.toString());
                User followee = new User(currentusername,latLng.longitude,latLng.latitude);
                reference.child(phonenumber).child("Followee").child(currentuserphoneNumber).child("Current location").setValue(followee);

                for(Iterator<Map.Entry<String, Checkpoint>> it = Checkpointsmap.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<String, Checkpoint> entry = it.next();
                    double distance = SphericalUtil.computeDistanceBetween( new LatLng(latLng.latitude,latLng.longitude), entry.getValue().latLng);
                    if (distance <100){
                        sendNotification(currentusername+" has arrived checkpoint "+ checkpointnumber.toString(),"I have arrived "+entry.getKey());
                        //Log.i("distance", String.valueOf(distance));
                        it.remove();
                        checkpointnumber++;
                    }
                }

                double distance = SphericalUtil.computeDistanceBetween(route.endLocation,new LatLng(latLng.latitude,latLng.longitude) );
                Log.i("distance", String.valueOf(distance));
                //Log.i("distancetoend", String.valueOf(distance));
                if (distance<20){

                    try{
                        reference.child(phonenumber).child("Followee").child(currentuserphoneNumber).child("Current location").child("name").setValue(currentusername);
                        sendNotification(currentusername+" is home","Thank you. I'm home!");
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    mTextField.setText("DONE");

                    mCountDownTimer.cancel();
                    context.stopService(new Intent(context, MyBackgroundService.class));
                    LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
                    //only current location shown -> stop broadcast

                };
            }
        };

        mTextField = findViewById( R.id.textView3 );
        auth = FirebaseAuth.getInstance();
        checkpointnumber=1;
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        Toast.makeText(this, "Have a safe journey!", Toast.LENGTH_SHORT).show();

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                ==PackageManager.PERMISSION_GRANTED){}
        else{ ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CALL_PHONE},
                24); }

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
        }


        
        mRequestQue = Volley.newRequestQueue(this);

        Intent myIntent=getIntent();
        time = myIntent.getStringExtra("Time");
        extratime = Long.parseLong(myIntent.getStringExtra("editExtratime"))*60*1000;
        phonenumber = myIntent.getStringExtra("phoneNumber").replaceAll(" ","");
        route = (Route) getIntent().getParcelableExtra("route");
        Checkpointsmap = (HashMap<String, Checkpoint>) myIntent.getSerializableExtra("Checkpointsmap");

        //geofencingClient = LocationServices.getGeofencingClient(this);
       // geofenceHelper = new GeofenceHelper(this);

        min = Long.parseLong(time)*60*1000 + extratime;
        Log.i("Display minutes:",String.valueOf(min));

        mTimeLeftinMillis = min;
        mButtonStartPause = findViewById(R.id.btnStart_pause);
        startTimer();

        mButtonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTimerRunning) {
                    pauseTimer();
                    sendNotification(currentusername+" pauses the journey","Hi. Just a short break!");
                }else{
                    startTimer();
                    sendNotification(currentusername+" continues the journey","I'm on my way!");
                }
            }
        });

        updateCountDownText();
        findViewById(R.id.phone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callfriend();
            }
        });

//        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                auth.signOut();
//                Intent myIntent = new Intent(CountdownActivity.this, MainActivity.class);
//                startActivity(myIntent);
//            }
//        });

        findViewById(R.id.SOSbtn).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                sendNotification(currentusername+" is in emergency","I need your help!");
                Toast.makeText(CountdownActivity.this,"You sent a SOS message to your follower.",Toast.LENGTH_SHORT).show();
                return false;
            }
        });



        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();



        currentuserphoneNumber = user.getPhoneNumber();

            reference.child(phonenumber).child("token").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    token = dataSnapshot.getValue().toString();
                    reference.child(currentuserphoneNumber).child("name").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            currentusername = dataSnapshot.getValue().toString();
                            sendNotification(currentusername+" starts the journey","Hi! I'm on my way.");
                            reference.child(phonenumber).child("Followee").child(currentuserphoneNumber).child("Route").setValue(route);
                            Log.i("Route pushed","good");
                            //reference.child(phonenumber).child("Followee").child(currentuserphoneNumber).child("Checkpoints").setValue(Checkpointsmap);
                            //Log.i("Checkpoints pushed","good");
                            //getlocation();
                            getLastKnownLocation();}


                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        LocalBroadcastManager.getInstance(CountdownActivity.this).registerReceiver(
                mMessageReceiver, new IntentFilter("location"));

    }
    private void startLocationService(){
        if(!isLocationServiceRunning()){
            serviceIntent = new Intent(this, MyBackgroundService.class);
//        this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

                CountdownActivity.this.startForegroundService(serviceIntent);
            }else{
                startService(serviceIntent);
            }
        }
    }
//    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            latLng =intent.getParcelableExtra("location"); //receibe
//            Log.i("latlongcountdown",latLng.toString());
//            User followee = new User(currentusername,latLng.longitude,latLng.latitude);
//            reference.child(phonenumber).child("Followee").child(currentuserphoneNumber).child("Current location").setValue(followee);
//
//            for(Iterator<Map.Entry<String, Checkpoint>> it = Checkpointsmap.entrySet().iterator(); it.hasNext(); ) {
//                Map.Entry<String, Checkpoint> entry = it.next();
//                double distance = SphericalUtil.computeDistanceBetween( new LatLng(latLng.latitude,latLng.longitude), entry.getValue().latLng);
//                if (distance <100){
//                    sendNotification(currentusername+" has arrived checkpoint "+ checkpointnumber.toString(),"I have arrived "+entry.getKey());
//                    Log.i("distance", String.valueOf(distance));
//                    it.remove();
//                    checkpointnumber++;
//                }
//            }
//
//            double distance = SphericalUtil.computeDistanceBetween(route.endLocation,new LatLng(latLng.latitude,latLng.longitude) );
//            //Log.i("distancetoend", String.valueOf(distance));
//            if (distance<10){
//
//                try{
//                    reference.child(phonenumber).child("Followee").child(currentuserphoneNumber).child("Current location").child("name").setValue(currentusername);
//                    sendNotification(currentusername+" is home","Thank you. I'm home!");
//                }
//                catch (Exception e) {
//                    e.printStackTrace();
//                }
//                mCountDownTimer.cancel();
//                context.stopService(new Intent(context, MyBackgroundService.class));
//                LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
//
//
//            };
//        }
//    };
    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.example.trackingapp.services.MyBackgroundService".equals(service.service.getClassName())) {
                Log.d("TAG", "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d("TAG", "isLocationServiceRunning: location service is not running.");
        return false;
    }

    private void sendNotification(String title,String body) {
        final JSONObject json = new JSONObject();
        try {
            json.put("to",token);
            Log.d("tokenofNga",token);
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title",title);
            notificationObj.put("body",body);
            json.put("notification",notificationObj);


            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL,
                    json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Log.d("MUR", "onResponse: "+json);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("MUR", "onError: "+error.networkResponse);
                }
            }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> header = new HashMap<>();
                    header.put("content-type","application/json");
                    header.put("authorization","key=AAAA443xiqY:APA91bFsgdpf40wV5nYgv9zcQZeP1rkQlksLTSrLPiey_Df_IVOm9p09aeBI0m4ZIIy58E8yfSqLCOnJ9SLZ4oHkiEc8J_h_mH1HXlxoV-M_Kp4zFNVm7PSkP6tfld3_E5_xabL5I_AZ");
                    return header;
                }
            };
            mRequestQue.add(request);
        }
        catch (JSONException e)

        {
            e.printStackTrace();
        }
    }

    private void getLastKnownLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    startLocationService();}
            }
        });

    }
//    public void getlocation(){
//        locationListener = new LocationListener() {
//            @Override
//            public void onLocationChanged(Location location) {
//                User followee = new User(currentusername,location.getLongitude(),location.getLatitude());
//                reference.child(phonenumber).child("Followee").child(currentuserphoneNumber).child("Current location").setValue(followee);
//
//                for(Iterator<Map.Entry<String, Checkpoint>> it = Checkpointsmap.entrySet().iterator(); it.hasNext(); ) {
//                    Map.Entry<String, Checkpoint> entry = it.next();
//                    double distance = SphericalUtil.computeDistanceBetween( new LatLng(location.getLatitude(),location.getLongitude()), entry.getValue().latLng);
//                    if (distance <100){
//                        sendNotification(currentusername+" has arrived checkpoint "+ checkpointnumber.toString(),"I have arrived "+entry.getKey());
//                       Log.i("distance", String.valueOf(distance));
//                       it.remove();
//                        checkpointnumber++;
//                    }
//                }
//
//                double distance = SphericalUtil.computeDistanceBetween(route.endLocation,new LatLng(location.getLatitude(),location.getLongitude()) );
//                //Log.i("distancetoend", String.valueOf(distance));
//                if (distance<10){
//
//                    locationManager.removeUpdates(locationListener);
//                    try{
//                        reference.child(phonenumber).child("Followee").child(currentuserphoneNumber).child("Current location").child("name").setValue(currentusername);
//                        sendNotification(currentusername+" is home","Thank you. I'm home!");
//                    }
//                    catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                    mCountDownTimer.cancel();
//
//                }
//
//
//
//            }
//
//            @Override
//            public void onStatusChanged(String provider, int status, Bundle extras) {
//
//            }
//
//            @Override
//            public void onProviderEnabled(String provider) {
//
//            }
//
//            @Override
//            public void onProviderDisabled(String provider) {
//
//            }
//        };
//
//        locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);
//        try {
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, locationListener);
//        }
//        catch (SecurityException e){
//            e.printStackTrace();
//        }
//    }

    private void startTimer() {
        mCountDownTimer = new CountDownTimer(mTimeLeftinMillis,1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftinMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                mButtonStartPause.setText("Finished");
                mTextField.setText("Time's out!");
                sendNotification(currentusername +"'s time runs out!","Time runs out.");
                //callfriend();

            }
        }.start();
        mTimerRunning = true;
        mButtonStartPause.setText("pause");

    }

    private void callfriend() {
        Intent intent=new Intent();
        intent.setAction(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phonenumber));
        startActivity(intent);
    }

    private void updateCountDownText() {
//        int minutes =(int) (mTimeLeftinMillis-extratime)/1000/60;
//        int seconds =(int) (mTimeLeftinMillis-extratime)/1000%60;
        //if (mTimeLeftinMillis+60*1000>=extratime){
            int minutes =(int) (mTimeLeftinMillis)/1000/60;
            int seconds =(int) (mTimeLeftinMillis)/1000%60;
            String timeLeftFormatted = String.format(Locale.getDefault(),"%02d:%02d",minutes,seconds);
            mTextField.setText(timeLeftFormatted);
//        }else{
//            int minutes =(int) (mTimeLeftinMillis-extratime)/1000/60;
//            int seconds =(int) (extratime-mTimeLeftinMillis)/1000%60;
//            String timeLeftFormatted = String.format(Locale.getDefault(),"%02d:%02d",minutes,seconds);
//            mTextField.setText(timeLeftFormatted);
//        }
    }

    private void pauseTimer() {
        mCountDownTimer.cancel();
        mTimerRunning=false;
        mButtonStartPause.setText("Start");

    }

    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        } else {
            //Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //We need to show user a dialog for displaying why the permission is needed and then ask for the permission...
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            } else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            }
        }
        if (Build.VERSION.SDK_INT >= 29) {
            //We need background permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    //We show a dialog and ask for permission
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 123);
                } else {
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 123);
                }
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1234) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
            } else {
                //We do not have the permission..

            }
        }

        if (requestCode == 10002) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                Toast.makeText(this, "You can add geofences...", Toast.LENGTH_SHORT).show();
            } else {
                //We do not have the permission..
                Toast.makeText(this, "Background location access is neccessary for geofences to trigger...", Toast.LENGTH_SHORT).show();
            }
        }
    }




}
