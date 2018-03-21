package com.monte.tangoapp.tasks;

import android.location.Location;

import com.monte.tangoapp.model.Elevation;
import com.monte.tangoapp.model.SparkFunWeather;
import com.monte.tangoapp.model.Weather;

import java.util.List;

/**
 * Created by monte on 05/01/2017.
 */
public interface TaskCompleteChecker {
    void onWeatherUpdated(Weather weather);
    void onLocationUpdated(Location location);
    void onSparkFunUpdated (List<SparkFunWeather> sparkFunWeatherList);
    void onElevationUpdated (Elevation elevation);

}
