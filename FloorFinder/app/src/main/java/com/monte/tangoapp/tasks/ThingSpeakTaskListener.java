package com.monte.tangoapp.tasks;

import com.monte.tangoapp.model.ThingSpeakWeather;

/**
 * Created by monte on 19/01/2017.
 */
public interface ThingSpeakTaskListener {
    void onThingSpeakUpdated(ThingSpeakWeather thingSpeakWeather);
}
