package com.monte.tangoapp.tasks;

import com.monte.tangoapp.model.SparkFunWeather;

import java.util.List;

/**
 * Created by monte on 05/01/2017.
 */
public interface SparkFunTaskListener {
    void onSparkFunUpdated (List<SparkFunWeather> sparkFunWeatherList);
}
