package com.monte.tangoapp.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.monte.tangoapp.HttpClientQuery;
import com.monte.tangoapp.JSONParser;
import com.monte.tangoapp.UserActivity;
import com.monte.tangoapp.model.SparkFunWeather;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by monte on 05/01/2017.
 */
public class JSONSparkFunPullTask extends AsyncTask<String, Void, List<SparkFunWeather>> {
    private List<SparkFunWeather> sparkFunWeatherList;
    private SparkFunTaskListener taskListener;

    public JSONSparkFunPullTask(SparkFunTaskListener taskListener) {
        this.taskListener = taskListener;
    }

    @Override
    protected List<SparkFunWeather> doInBackground(String... params) {
        //data is received as a json string from the requested website
        String data = ((new HttpClientQuery()).getQueryResult(params[0]));
        List<SparkFunWeather> weatherList = new ArrayList<>();
        try {
            //then data is parsed using a json parser into an elevation object
            weatherList = JSONParser.getSparkFunWeatherResults(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //returns the received elevation object
        return weatherList;
    }

    @Override
    protected void onPostExecute(List<SparkFunWeather> sparkFunWeatherList) {
        super.onPostExecute(sparkFunWeatherList);
        this.sparkFunWeatherList = sparkFunWeatherList;
//        for (SparkFunWeather sparkFunWeather: sparkFunWeatherList) {
//            Log.e("spark", sparkFunWeather.getLocation() + " " + sparkFunWeather.getPressureGroundLevel() + " " + sparkFunWeather.getUnixTime());
//        }
        taskListener.onSparkFunUpdated(sparkFunWeatherList);
    }

    public List<SparkFunWeather> getSparkFunWeatherList() {
        return sparkFunWeatherList;
    }
}