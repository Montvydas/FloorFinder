package com.monte.pressurestation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import org.json.JSONException;

/**
 * Created by monte on 19/12/2016.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        Intent background = new Intent(context, BackgroundService.class);

//        context.startService(background);
        Log.i("SimpleWakefulReceiver", "" + System.currentTimeMillis());
    }
}