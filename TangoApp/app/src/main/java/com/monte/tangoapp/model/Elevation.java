package com.monte.tangoapp.model;

/**
 * Created by Monty on 19/06/2016.
 */

//Elevation class object holds the information about the google elevation service results
public class Elevation {
    private float altitude;     //query gives altitude/elevation
    private float resolution;   //resolution
    private String status;      //and status ("OK")

    //getters and setters are used specify the data when json is extracted and then
    //use it within the application
    public float getAltitude() {
        return altitude;
    }
    public float getResolution() {
        return resolution;
    }
    public String getStatus() {
        return status;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }
    public void setResolution(float resolution) {
        this.resolution = resolution;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
