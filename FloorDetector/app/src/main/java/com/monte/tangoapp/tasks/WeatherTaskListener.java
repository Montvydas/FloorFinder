package com.monte.tangoapp.tasks;

import com.monte.tangoapp.model.Weather;

/**
 * Created by monte on 05/01/2017.
 */
public interface WeatherTaskListener {
    void onWeatherUpdated(Weather weather);
}
