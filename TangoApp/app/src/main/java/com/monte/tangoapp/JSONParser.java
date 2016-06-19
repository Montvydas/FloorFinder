package com.monte.tangoapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.monte.tangoapp.model.Elevation;
import com.monte.tangoapp.model.Weather;

public class JSONParser {

    public static Weather getOpenWeatherMapWeather(String data) throws JSONException  {
        Weather weather = new Weather();
        JSONObject jObj = new JSONObject(data);

        weather.setUnixTime(getLong("dt", jObj));
        JSONObject mainObj = getObject("main", jObj);
        weather.setTemperature(getFloat("temp", mainObj));
        weather.setHumidity(getFloat("humidity", mainObj));
        weather.setPressureSea(getFloat("sea_level", mainObj));

        return weather;
    }

    public static Weather getForecastWeather (String data) throws JSONException {
        Weather weather = new Weather();
        JSONObject jObj = new JSONObject(data);

        JSONObject currentlyObj = getObject("currently", jObj);
        weather.setUnixTime(getLong("time", currentlyObj));
        weather.setTemperature(getFloat("temperature", currentlyObj)+273.15f);  //Absolute temperature
        weather.setHumidity(getFloat("humidity", currentlyObj) * 100);
        weather.setPressureSea(getFloat("pressure", currentlyObj));

        return weather;
    }

    public static Elevation getGoogleElevationResults (String data) throws  JSONException {
        Elevation elevation  = new Elevation();
        JSONObject jObj = new JSONObject(data);

        JSONArray resultsArray = getArray("results", jObj);
        JSONObject resultsObj = resultsArray.getJSONObject(0);
        elevation.setAltitude(getFloat("elevation", resultsObj));
        elevation.setResolution(getFloat("resolution", resultsObj));

        elevation.setStatus(getString("status", jObj));
        return elevation;
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

}