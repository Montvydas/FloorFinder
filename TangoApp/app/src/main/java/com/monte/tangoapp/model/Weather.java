package com.monte.tangoapp.model;

//Weather class object holds the information about the weather results, coming from queries
public class Weather {
    private float humidity;         //currently saving humidity data
    private float temperature;      //temperature
    private float pressureSeaLevel; //pressure at sea level
    private long unixTime;          //and unix time, when the weather was updated

    //getters and setters are used specify the data when json is extracted and then
    //use it within the application

    public long getUnixTime() {
        return unixTime;
    }
    public void setUnixTime(long unixTime) {
        this.unixTime = unixTime;
    }
    public float getPressureSeaLevel() {
        return pressureSeaLevel;
    }
    public void setPressureSeaLevel(float pressureSeaLevel) {
        this.pressureSeaLevel = pressureSeaLevel;
    }
    public float getTemperature() {
        return temperature;
    }
    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }
    public float getHumidity() {
            return humidity;
        }
    public void setHumidity(float humidity) {
            this.humidity = humidity;
        }
}