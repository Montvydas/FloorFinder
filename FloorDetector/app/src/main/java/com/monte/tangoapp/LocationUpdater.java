package com.monte.tangoapp;

import android.*;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.location.LocationListener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.monte.tangoapp.model.Elevation;
import com.monte.tangoapp.model.SparkFunWeather;
import com.monte.tangoapp.model.Weather;
import com.monte.tangoapp.tasks.LocationTaskListener;
import com.monte.tangoapp.tasks.TaskCompleteChecker;

import java.util.List;

/**
 * Created by monte on 05/01/2017.
 */
public class LocationUpdater implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private Context context;
    GoogleApiClient mGoogleApiClient;
    private Location currentLocation;
    private int ACCURATE_DISTANCE = 40;


    private LocationTaskListener taskListener;
    public LocationUpdater(Context context, LocationTaskListener taskListener) {
        this.context = context;
        this.taskListener = taskListener;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        taskListener.onConnected();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
//        if (location.getAccuracy() > ACCURATE_DISTANCE){
//            currentLocation = location;
//            lat = currentLocation.getLatitude();
//            lon = currentLocation.getLongitude();
                //print latitude, longitude and accuracy

//            String countryCode = myCountryOffset.getCountryCode(this, lat, lon);
//            if (countryCode == null){
//                countryCode = "UK";
//            }
//            myCountryOffset.setFloorOffsetFromCountry(countryCode);
//        }
//        Log.e("location", location.getLatitude() + " " + location.getLongitude());
        taskListener.onLocationUpdated(location);
    }

    /**
     * Location updates are being started with specific parameters such as update interval
     */
    public void startLocationUpdates() {
        if (mGoogleApiClient != null){
            if (mGoogleApiClient.isConnected()){
                LocationRequest mLocationRequest = new LocationRequest();   //create location request object
                mLocationRequest.setInterval(500);                        //specify request interval
                mLocationRequest.setFastestInterval(500);                  //request location updates every .5 seconds at fastest
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);   //force to use GPS location for the best accuracy

                //check if the user gave the permission to access the device location

                //check again if the client was successfully created
                //and then request location to be updated
                if (ActivityCompat.checkSelfPermission(this.context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        }
    }

    /**
     * Location provider - GoogleApiClient is created
     */
    public void createLocationProvider() {
        //Allows an app to access precise location, thus ask for permissions for that
        if (ActivityCompat.checkSelfPermission(this.context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this.context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        } else {
            //If permissions are already given
            //Create an instance of GoogleAPIClient
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this.context)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
            }
        }
    }

    //the method removes location updates
    public void stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    public void connectLocationProvider (){
        if (mGoogleApiClient != null) {   //start location service by connecting to it
            mGoogleApiClient.connect();
        }
    }

    public void disconnectLocationProvider (){
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();  //then app stops, disconnect from google api client
        }
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }
}
