package com.monte.tangoapp.model;

/**
 * Created by monty on 19/06/2016.
 */
public class Elevation {
    private float altitude;
    private float resolution;
    private String status;

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
