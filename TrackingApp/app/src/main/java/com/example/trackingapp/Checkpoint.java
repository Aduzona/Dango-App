package com.example.trackingapp;

import android.os.Parcel;
import android.os.Parcelable;


import com.example.trackingapp.Route;
import com.google.android.gms.maps.model.LatLng;
import com.example.trackingapp.Distance;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;


public class Checkpoint implements Parcelable {
    public String Address;
    public LatLng latLng;

    public Checkpoint(String address, LatLng latLng) {
        Address = address;
        this.latLng = latLng;
    }



    public Checkpoint() {
    }



    protected Checkpoint(Parcel in) {
        Address = in.readString();
        latLng = in.readParcelable(LatLng.class.getClassLoader());
    }

    public static final Creator<Checkpoint> CREATOR = new Creator<Checkpoint>() {
        @Override
        public Checkpoint createFromParcel(Parcel in) {
            return new Checkpoint(in);
        }

        @Override
        public Checkpoint[] newArray(int size) {
            return new Checkpoint[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Address);
        dest.writeParcelable(latLng, flags);
    }
}