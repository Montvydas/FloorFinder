package com.monte.pressurestation;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Create by Monte 2016/06.
 */

//JSON parser is used to parse json data from various queries to a sensible information
public class JSONParser {
    public static SparkFunPostStatus getSparkFunPostStatus (String data) throws JSONException {
        SparkFunPostStatus status  = new SparkFunPostStatus();
        JSONObject jObj = new JSONObject(data);
        status.setStatus(getBoolean("success", jObj));
        status.setMessage(getString("message", jObj));
        return status;
    }

    private static String getString(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getString(tagName);
    }

    private static boolean  getBoolean(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getBoolean(tagName);
    }

}