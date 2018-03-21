package com.monte.tangoapp.model;

/**
 * Created by monte on 18/12/2016.
 */
//Weather class object holds the information about the weather results, coming from queries
public class SparkFunWeather {
    private float pressureGroundLevel; //pressure at sea level
    private long unixTime;          //and unix time, when the weather was updated
    private String location;

    //getters and setters are used specify the data when json is extracted and then
    //use it within the application

    public long getUnixTime() {
        return unixTime;
    }
    public void setUnixTime(long unixTime) {
        this.unixTime = unixTime;
    }
    public float getPressureGroundLevel() {
        return pressureGroundLevel;
    }
    public void setPressureGroundLevel(float pressureGroundLevel) {
        this.pressureGroundLevel = pressureGroundLevel;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
}