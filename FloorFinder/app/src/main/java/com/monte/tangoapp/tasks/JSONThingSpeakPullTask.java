package com.monte.tangoapp.tasks;

import android.os.AsyncTask;

import com.monte.tangoapp.HttpClientQuery;
import com.monte.tangoapp.JSONParser;
import com.monte.tangoapp.model.SparkFunWeather;
import com.monte.tangoapp.model.ThingSpeakWeather;

import org.json.JSONException;

import java.util.List;

/**
 * Created by monte on 19/01/2017.
 */
public class JSONThingSpeakPullTask extends AsyncTask<String, Void, ThingSpeakWeather> {
    private ThingSpeakWeather thingSpeakWeather;
    private ThingSpeakTaskListener taskListener;

    public JSONThingSpeakPullTask(ThingSpeakTaskListener taskListener) {
        this.taskListener = taskListener;
    }

    @Override
    protected ThingSpeakWeather doInBackground(String... params) {
        //data is received as a json string from the requested website
        String data = ((new HttpClientQuery()).getQueryResult(params[0]));
        ThingSpeakWeather stationData = new ThingSpeakWeather();
        try {
            //then data is parsed using a json parser into an elevation object
            stationData = JSONParser.getThingerSpeakWeatherResults(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //returns the received elevation object
        return stationData;
    }

    @Override
    protected void onPostExecute(ThingSpeakWeather thingSpeakWeather) {
        super.onPostExecute(thingSpeakWeather);
        this.thingSpeakWeather = thingSpeakWeather;
//        for (SparkFunWeather sparkFunWeather: sparkFunWeatherList) {
//            Log.e("spark", sparkFunWeather.getLocation() + " " + sparkFunWeather.getPressureGroundLevel() + " " + sparkFunWeather.getUnixTime());
//        }
        taskListener.onThingSpeakUpdated(thingSpeakWeather);
    }

    public ThingSpeakWeather getSparkFunWeatherList() {
        return thingSpeakWeather;
    }
}
