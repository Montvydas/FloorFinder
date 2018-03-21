package com.monte.tangoapp.tasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.monte.tangoapp.HttpClientQuery;
import com.monte.tangoapp.JSONParser;
import com.monte.tangoapp.UserActivity;
import com.monte.tangoapp.model.Weather;

import org.json.JSONException;

/**
 * Created by monte on 05/01/2017.
 */
//A request is made in a form of async task to perform in background
public class JSONWeatherTask extends AsyncTask<String, Void, Weather> {
    private Weather weather;
    private WeatherTaskListener taskListener;

    public JSONWeatherTask(WeatherTaskListener taskListener) {
        this.taskListener = taskListener;
    }

    @Override
    protected Weather doInBackground(String... params) {
        //data is received as a json string from the requested website
        String data = ((new HttpClientQuery()).getQueryResult(params[0]));
        Log.e("weather link", params[0]);
        Weather weather = new Weather();
        try {
            //then data is parsed using a json parser into a weather object
            weather = JSONParser.getForecastWeather(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //return the parsed weather object
        return weather;
    }

    //after the request is done, this method is being performed
    @Override
    protected void onPostExecute(Weather weather) {
        super.onPostExecute(weather);
        //weather object is being updated with a new weather
        this.weather = weather;
        taskListener.onWeatherUpdated(weather);
    }

    public Weather getWeather() {
        return weather;
    }
}