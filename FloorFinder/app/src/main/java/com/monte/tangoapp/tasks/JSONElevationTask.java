package com.monte.tangoapp.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.monte.tangoapp.HttpClientQuery;
import com.monte.tangoapp.JSONParser;
import com.monte.tangoapp.UserActivity;
import com.monte.tangoapp.model.Elevation;

import org.json.JSONException;

/**
 * Created by monte on 05/01/2017.
 */
public class JSONElevationTask extends AsyncTask<String, Void, Elevation> {
    private Elevation elevation;
    private ElevationTaskListener taskListener;

    public JSONElevationTask(ElevationTaskListener taskListener) {
        this.taskListener = taskListener;
    }

    @Override
    protected Elevation doInBackground(String... params) {
        //data is received as a json string from the requested website
        String data = ((new HttpClientQuery()).getQueryResult(params[0]));
        Elevation elevation = new Elevation();
        try {
            //then data is parsed using a json parser into an elevation object
            elevation = JSONParser.getGoogleElevationResults(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //returns the received elevation object
        return elevation;
    }

    //after the request is done, this method is being performed
    @Override
    protected void onPostExecute(Elevation elevation) {
        super.onPostExecute(elevation);
        this.elevation = elevation;
        taskListener.onElevationUpdated(elevation);
    }

    public Elevation getElevation() {
        return elevation;
    }
}