package com.monte.indoordetection;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GpsStatus.Listener,LocationListener {
    private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private TextView infoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        infoText = (TextView)(findViewById(R.id.infoText));
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            return;
        } else {


            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            locationManager.addGpsStatusListener(this);
        }


    }

    LocationManager locationManager = null;
    List<GpsSatellite> satelliteList = new ArrayList<>();

    @Override
    public void onGpsStatusChanged(int event) {
        GpsStatus gpsStatus = locationManager.getGpsStatus(null);
        if(gpsStatus != null) {
            satelliteList.clear();
            for (GpsSatellite sat : gpsStatus.getSatellites()){
                if (sat.usedInFix()){
                    satelliteList.add(sat);
                }
            }
            printStatistics(satelliteList);
        }
    }

    private double prevMean = 0;
    private double prevStd = 0;
    private int prevCnt = 0;
    List<String> infoList = new ArrayList<>();

    private void printStatistics (List<GpsSatellite> satellites){
        Log.e("sat size", satellites.size() + "");

        int size = 0;
        for (GpsSatellite sat : satellites){
            if (sat.getSnr() > 1.0){
                size++;
            }
        }

        if (size == 0) {
            infoText.setText("No satellites found");
            return;
        }

        double[] snr = new double[size];
        for (int i = 0; i < snr.length; i++){
            if (satellites.get(i).getSnr() > 1.0){
                snr[i] = satellites.get(i).getSnr();
            }

        }

        Arrays.sort(snr);

        String finalText = "";
        for (int i = 0; i < snr.length; i++){
            finalText += String.format("%.2f", snr[i]) + "\n";
        }

        Statistics statistics = new Statistics(snr);
        double currMean = statistics.getMean();
        double currStd = statistics.getStdDev();
        int currCnt = snr.length;

        String currText = String.format("   cnt=%d     mean=%.2f     std=%.2f", currCnt, currMean, currStd);
        String prevText = String.format("∆cnt=%d    ∆mean=%.2f  ∆std=%.2f", (currCnt-prevCnt), (currMean-prevMean), (currStd-prevStd));

        infoList.add(currText + "\n" + prevText + "\n");
        if (infoList.size() > 3){
            infoList.remove(0);
        }

        for (String value : infoList){
            finalText += String.valueOf(value)+"\n";
        }
        
        infoText.setText(finalText);
        Log.e("statistics", prevText + "\n" + currText);

        prevCnt = currCnt;
        prevMean = currMean;
        prevStd = currStd;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish();
                    Log.e("permission", "Can't get access to location");
                }
                return;
            }
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.e("location", "changed");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
