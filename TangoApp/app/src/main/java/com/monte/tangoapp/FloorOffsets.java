package com.monte.tangoapp;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.monte.tangoapp.model.Elevation;
import com.monte.tangoapp.tasks.AddressTaskListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by monte on 30/12/2016.
 */
public class FloorOffsets {
    private int groundFloorOffset = 0;
    private int basementFloorOffset = 0;
    private Map<String, Pair<Integer, Integer>> offsetMap = new HashMap<String, Pair<Integer, Integer>>();
    private Address address;

    private void addCountriesToMap(){
        Pair<Integer, Integer> convention1 = new Pair<>(0, 0);
        Pair<Integer, Integer> convention2 = new Pair<>(0, 1);
        Pair<Integer, Integer> convention3 = new Pair<>(-1, 1);

        offsetMap.put("UK", convention1);
        offsetMap.put("LT", convention2);
        offsetMap.put("RU", convention3);
        offsetMap.put("US", convention2);
    }

    public FloorOffsets(){
        addCountriesToMap();
    }

    public void setFloorOffsetFromCountry(String countryCode){
        Pair<Integer, Integer> offsetPair = offsetMap.get(countryCode);
        if (offsetPair != null){
            this.groundFloorOffset = offsetPair.second;
            this.basementFloorOffset = offsetPair.first;
        } else {
            this.groundFloorOffset = 0;
            this.basementFloorOffset = 0;
        }
    }

    public int getGroundFloorOffset() {
        return groundFloorOffset;
    }

    public int getBasementFloorOffset() {
        return basementFloorOffset;
    }

    public static Address getLocationAddress (Context context, double latitude, double longitude) {
        Address locationAddress = null;
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                locationAddress = addresses.get(0);
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }
        return locationAddress;
    }

    public int getCountryFloor(int calculatedFloor){
        if (calculatedFloor >= 0){
            calculatedFloor += groundFloorOffset;
        } else {
            calculatedFloor += basementFloorOffset;
        }
        return calculatedFloor;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}