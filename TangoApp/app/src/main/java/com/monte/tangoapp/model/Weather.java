package com.monte.tangoapp.model;

public class Weather {
    private float humidity;
    private float temperature;
    private float pressureSeaLevel;
    private long unixTime;

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