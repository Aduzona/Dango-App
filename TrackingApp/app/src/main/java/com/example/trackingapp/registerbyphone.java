package com.example.trackingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class registerbyphone extends AppCompatActivity {


    EditText editTextPhone, editTextCode,editTextName;

    FirebaseAuth mAuth;
    FirebaseUser user;
    DatabaseReference reference;
    String userId;
    String phone;
    String codeSent,token;
    CountryCodePicker ccp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registerbyphone);
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        editTextCode = findViewById(R.id.editTextCode);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextName =   findViewById(R.id.Name);

        ccp.registerCarrierNumberEditText(editTextPhone);
        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                editTextPhone.getText().clear();
                //ccp.setCountryForPhoneCode();

            }
        });
        getpermission();
        gettoken();// get phone token
        findViewById(R.id.buttonGetVerificationCode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode();
            }
        });

        ccp.setPhoneNumberValidityChangeListener(new CountryCodePicker.PhoneNumberValidityChangeListener() {
            @Override
            public void onValidityChanged(boolean isValidNumber) {
                sendVerificationCode();
            }
        });

        findViewById(R.id.buttonSignIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifySignInCode();
            }
        });
    }

    private void verifySignInCode(){
        String code = editTextCode.getText().toString();

        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);
            signInWithPhoneAuthCredential(credential);
        }catch (Exception e){
            Toast toast = Toast.makeText(getApplicationContext(), "Incorrect Verification Code ", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(),
                                "Login Successfully", Toast.LENGTH_LONG).show();

                            //user = mAuth.getCurrentUser();
                            reference.child(phone.replaceAll(" ","") )
                                    .child("name")
                                    .setValue(editTextName.getText().toString());
                            reference.child(phone.replaceAll(" ","") )
                                    .child("token")
                                    .setValue(token);
                            Intent myIntent = new Intent(registerbyphone.this, UserLocationMainActivity.class);
                            startActivity(myIntent);
                            finish();



                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(getApplicationContext(),
                                        "Incorrect Verification Code ", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void sendVerificationCode(){

        ccp.isValidFullNumber();
        phone = ccp.getFormattedFullNumber();
                //editTextPhone.getText().toString();
        //editTextPhone.setText(phone.replaceAll(" ",""));


        if(phone.isEmpty()){
            editTextPhone.setError("Phone number is required");
            editTextPhone.requestFocus();
            return;
        }

        if(phone.length() < 10 ){
            editTextPhone.setError("Please enter a valid phone");
            editTextPhone.requestFocus();
            return;
        }


        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,        // Phone number to verify
                10,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }



    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();

            Log.d("REGISTER_USER", "code generated first " + code);
            if (code != null) {
                //verifying the code like in normal flow
                editTextCode.setText(code);
                verifySignInCode();
            } else {
                //you dont get any code, it is instant verification
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            codeSent = s;
        }
    };

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

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.SYSTEM_ALERT_WINDOW)
                == PackageManager.PERMISSION_GRANTED) {
        }else{ ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW},
                23);
        }


        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();

        }
    }
    public void gettoken(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }

                        // Get new Instance ID token
                        token = task.getResult().getToken();

                        // Log and toast
                        Log.i("xyz",token);
                    }
                });
    }




}

