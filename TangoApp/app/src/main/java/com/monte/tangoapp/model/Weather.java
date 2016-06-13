package com.monte.tangoapp.model;

public class Weather {
    private float pressure;
    private float humidity;
    private float temperature;
    private float pressureSeaLevel;
    private float unixTime;

    public float getUnixTime() {
        return unixTime;
    }
    public void setUnixTime(float unixTime) {
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
    public float getPressure() {
            return pressure;
        }
    public void setPressure(float pressure) {
            this.pressure = pressure;
        }
    public float getHumidity() {
            return humidity;
        }
    public void setHumidity(float humidity) {
            this.humidity = humidity;
        }
}