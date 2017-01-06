package com.monte.tangoapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.monte.tangoapp.model.Elevation;
import com.monte.tangoapp.model.SparkFunPostStatus;
import com.monte.tangoapp.model.SparkFunWeather;
import com.monte.tangoapp.model.Weather;

import java.util.ArrayList;
import java.util.List;

/**
 * Create by Monte 2016/06.
 */

//JSON parser is used to parse json data from various queries to a sensible information
public class JSONParser {

    //OpenWeather query parser
    public static Weather getOpenWeatherMapWeather(String data) throws JSONException  {
        Weather weather = new Weather();
        JSONObject jObj = new JSONObject(data);

        //gets the required json objects and extracts the relevant information
        weather.setUnixTime(getLong("dt", jObj));
        JSONObject mainObj = getObject("main", jObj);
        weather.setTemperature(getFloat("temp", mainObj));
        weather.setHumidity(getFloat("humidity", mainObj));
        weather.setPressureSeaLevel(getFloat("sea_level", mainObj));

        return weather;
    }

    //Forecast.io query parser
    public static Weather getForecastWeather (String data) throws JSONException {
        Weather weather = new Weather();
        if (data == null){
            return null;
        }
        JSONObject jObj = new JSONObject(data);
        //gets the required json objects and extracts the relevant information
        JSONObject currentlyObj = getObject("currently", jObj);
        weather.setUnixTime(getLong("time", currentlyObj));
        weather.setTemperature(getFloat("temperature", currentlyObj)+273.15f);  //Absolute temperature
        weather.setHumidity(getFloat("humidity", currentlyObj) * 100);
        weather.setPressureSeaLevel(getFloat("pressure", currentlyObj));

        return weather;
    }

    //Google Elevation Service query parser
    public static Elevation getGoogleElevationResults (String data) throws  JSONException {
        Elevation elevation  = new Elevation();
        JSONObject jObj = new JSONObject(data);

        //gets the required json objects and extracts the relevant information
        JSONArray resultsArray = getArray("results", jObj);
        JSONObject resultsObj = resultsArray.getJSONObject(0);
        elevation.setAltitude(getFloat("elevation", resultsObj));
        elevation.setResolution(getFloat("resolution", resultsObj));

        elevation.setStatus(getString("status", jObj));
        return elevation;
    }

    public static List<SparkFunWeather> getSparkFunWeatherResults (String data) throws JSONException{
        List<SparkFunWeather> weatherList = new ArrayList<>();
        if (data.equals(null))
            return weatherList;
        JSONArray jArray = new JSONArray(data);
        for (int i = 0; i < jArray.length(); i++) {
            JSONObject jObj = jArray.getJSONObject(i);
            SparkFunWeather weather = new SparkFunWeather();
            weather.setLocation(getString("location", jObj));
            weather.setPressureGroundLevel(getFloat("pressure", jObj));
            weather.setUnixTime(getLong("time", jObj));
            weatherList.add(weather);
        }
        return weatherList;
    }

    public static SparkFunPostStatus getSparkFunPostStatus (String data) throws JSONException {
        SparkFunPostStatus status  = new SparkFunPostStatus();
        JSONObject jObj = new JSONObject(data);
        status.setStatus(getBoolean("success", jObj));
        status.setMessage(getString("message", jObj));
        return status;
    }


    private static JSONObject getObject(String tagName, JSONObject jObj)  throws JSONException {
        JSONObject subObj = jObj.getJSONObject(tagName);
        return subObj;
    }

    private static JSONArray getArray (String tagName, JSONObject jObj) throws JSONException {
        JSONArray subArray = jObj.getJSONArray(tagName);
        return  subArray;
    }

    private static String getString(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getString(tagName);
    }

    private static float  getFloat(String tagName, JSONObject jObj) throws JSONException {
        return (float) jObj.getDouble(tagName);
    }

    private static long getLong (String tagName, JSONObject jObj) throws JSONException {
        return jObj.getLong(tagName);
    }
    private static int  getInt(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getInt(tagName);
    }
    private static boolean  getBoolean(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getBoolean(tagName);
    }

}