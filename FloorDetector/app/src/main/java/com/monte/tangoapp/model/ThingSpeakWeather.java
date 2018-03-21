package com.monte.tangoapp.model;

/**
 * Created by monte on 19/01/2017.
 */
public class ThingSpeakWeather {
    private float pressureGroundLevel; //pressure at sea level
    private long unixTime;          //and unix time, when the weather was updated
    private String location;
    private String entryId;
    private String createdAt;

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

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
