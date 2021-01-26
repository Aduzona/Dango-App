package com.example.trackingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.HashMap;

public class JourneyInfo extends AppCompatActivity {

    EditText Time,t1;
    EditText editExtratime;
    TextView phoneNumber;
    String address,requiredtime,phone;
    Route route;
    HashMap<String, Checkpoint> hashMapObject;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_info);

        t1 =(EditText)findViewById(R.id.editText9); //address
        phoneNumber = findViewById(R.id.textView4);
        Time = findViewById(R.id.Time);
        editExtratime = findViewById(R.id.editExtratime);

        Intent myIntent=getIntent();

        if(myIntent!=null){
            route = (Route) getIntent().getParcelableExtra("route");
            hashMapObject = (HashMap<String, Checkpoint>) myIntent.getSerializableExtra("Checkpointsmap");

        }

        //Time.setText(requiredtime);
        t1.setText(route.endAddress);
        //choose contact from contact list
        findViewById(R.id.Contactbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(myIntent, 1);
            }
        });

        //Move to another screen
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phone = phoneNumber.getText().toString().replaceAll(" ","");
                if(phone.isEmpty()){
                    phoneNumber.setError("Phone number is required");
                    phoneNumber.requestFocus();
                    return;
                }

                if(phone.length() < 10 ){
                    phoneNumber.setError("Please enter a valid phone");
                    phoneNumber.requestFocus();
                    return;
                }

                //check if phonenumber in right format
                if (phone.charAt(0) == '0'){
                    phone = "+49"+phone.substring(1);
                    Log.i("assaf",phone);
                }else{
                    Log.i("asdad",phone.substring(0, 1));
                }
                //check if follower has registered the app, if yes, move to another screen
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users");
                rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.hasChild(phone)) {

                            String timeValue= Time.getText().toString();
                            int timeInt= Integer.parseInt(timeValue);
//                            String extraTimeValue= Time.getText().toString();
//                            int extraTimeInt= Integer.parseInt(extraTimeValue);

//                            int newTime=timeInt + extraTimeInt;
                            if(haveNetworkConnection()){
                                Intent myIntent = new Intent(JourneyInfo.this, CountdownActivity.class);

                                myIntent.putExtra("route",route);
                                Bundle extras = new Bundle();
                                extras.putSerializable("Checkpointsmap", (Serializable) hashMapObject);
                                myIntent.putExtras(extras);
                                myIntent.putExtra("route",route);

                                myIntent.putExtra("Time",Integer.toString(timeInt));
                                myIntent.putExtra("editExtratime",editExtratime.getText().toString());
                                myIntent.putExtra("phoneNumber",phone );

                                startActivity(myIntent);
                                finish();
                            }else{
                                Toast.makeText(JourneyInfo.this,"Please enable your Internet connection",Toast.LENGTH_SHORT).show();
                            }

                        }else{
                            Toast.makeText(JourneyInfo.this,"This phone number is not registered",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK){
            switch (requestCode) {
                case 1:
                    contactPicked(data);
                    break;
            }

        }else{
            Toast.makeText(this,"Failed to pick contact",Toast.LENGTH_SHORT).show();
        }
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
    private void contactPicked(Intent data) {
        Cursor cursor = null;
        try{
            String phone = null;
            Uri uri = data.getData();
            cursor = getContentResolver().query(uri,null,null,null,null);
            cursor.moveToFirst();
            int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            phone = cursor.getString(phoneIndex);
            phoneNumber.setText(phone);

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
