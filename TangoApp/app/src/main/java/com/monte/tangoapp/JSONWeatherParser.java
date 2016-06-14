package com.monte.tangoapp;
/**
 * This is a tutorial source code
 * provided "as is" and without warranties.
 *
 * For any question please visit the web site
 * http://www.survivingwithandroid.com
 *
 * or write an email to
 * survivingwithandroid@gmail.com
 *
 */

        import org.json.JSONException;
        import org.json.JSONObject;

        import com.monte.tangoapp.model.Weather;

/*
 * Copyright (C) 2013 Surviving with Android (http://www.survivingwithandroid.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class JSONWeatherParser {

    public static Weather getWeather(String data) throws JSONException  {
        Weather weather = new Weather();
        JSONObject jObj = new JSONObject(data);

//        JSONObject dtObj = getObject("dt", jObj);

        weather.setUnixTime(getString("dt", jObj));

        JSONObject mainObj = getObject("main", jObj);
        weather.setPressure(getFloat("pressure", mainObj));
        weather.setTemperature(getFloat("temp", mainObj));
        weather.setHumidity(getFloat("humidity", mainObj));
        weather.setPressureSeaLevel(getFloat("sea_level", mainObj));

        return weather;
    }


    private static JSONObject getObject(String tagName, JSONObject jObj)  throws JSONException {
        JSONObject subObj = jObj.getJSONObject(tagName);
        return subObj;
    }

    private static String getString(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getString(tagName);
    }

    private static float  getFloat(String tagName, JSONObject jObj) throws JSONException {
        return (float) jObj.getDouble(tagName);
    }

//    private static int  getInt(String tagName, JSONObject jObj) throws JSONException {
//        return jObj.getInt(tagName);
//    }

}