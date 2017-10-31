package com.monte.pressurestation;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor pressureSensor;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initialiseSensors();

        this.context = this;
        Intent alarm = new Intent(this.context, MyAlarmReceiver.class);
        boolean alarmRunning = (PendingIntent.getBroadcast(this.context, 0, alarm, PendingIntent.FLAG_NO_CREATE) != null);

        if(alarmRunning == false) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.context, 0, alarm, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//            if(Build.VERSION.SDK_INT < 23){
//                if(Build.VERSION.SDK_INT >= 19){
//                    setExact(...);
//                }
//                else{
//                    set(...);
//                }
//            }
//            else{
//                setExactAndAllowWhileIdle(...);
//            }

            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, System.currentTimeMillis(), 600, pendingIntent);
//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, 10000, pendingIntent);
        }

//        AlarmManager alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, YourAlarmReceiver.class),PendingIntent.FLAG_CANCEL_CURRENT);

// Use inexact repeating which is easier on battery (system can phase events and not wake at exact times)
//        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, System.currentTimeMillis(), 10000, pendingIntent);

        startRepeatedUpdates();
        sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    public class YourAlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("PressureStation", "waking up!!!");
//            context.startService(new Intent(context, YourService.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String BASE_URL_SPARK= "http://data.sparkfun.com/";
    private  String API_KEY_PUBLIC_SPARK = "***REMOVED***";
    private String API_KEY_PRIVATE_SPARK = "***REMOVED***";

    private String[] getSparkFunPushUrl (String baseAddress, String apiPublicKey, String apiPrivateKey, float pressure){
        long unixTime = System.currentTimeMillis() / 1000L;
        return new String[] {baseAddress + "input/" + apiPublicKey + ".json?private_key=" + apiPrivateKey +
                "&location=" + "edinburgh" + "&pressure=" + String.format("%.3f", pressure) + "&time=" + unixTime};
    }

    private String BASE_URL_THINGSPEAK = "https://api.thingspeak.com/";
    private String API_KEY_PRIVATE_THINGSPEAK = "***REMOVED***";

    private String[] getThingSpeakPushUrl (String baseAddress, String apiPrivateKey, float pressure){
        long unixTime = System.currentTimeMillis() / 1000L;
        return new String[] {baseAddress + "update?api_key=" + apiPrivateKey + "&field1=" + "edinburgh" +
                "&field2=" + String.format("%.3f", pressure) + "&field3=" + unixTime};
    }


    /**
     * All of the used sensors are initialised and checked if they exist in the application.
     */
    public void initialiseSensors (){
        //Get an instance of a sensor manager
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        //Use sensor manager to request pressure sensor object
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        //check if the phone has an embedded pressure sensor
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) == null){
            Toast.makeText(this, "Phone doesn't have Pressure sensor", Toast.LENGTH_SHORT).show();// Success! There's a pressure sensor.
            try {
                //No reason to run the app if the phone doesn't have a pressure sensor
                Thread.sleep(1000);
                finish();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        stopRepeatingUpdates();
    }

    private final float filterSmooth = 0.2f;
    private float millibars_of_pressure = 1013.25f;
    private float sea_level_pressure = 1013.25f;

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_PRESSURE:  //if it was pressure, get the pressure value
                float currPressure = event.values[0]*filterSmooth + millibars_of_pressure*(1-filterSmooth);
                millibars_of_pressure = currPressure;
                sea_level_pressure = currPressure + 9.64f;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public void pushDataButton (View v){
        if (checkConnectivity()) {
//            new JSONSparkFunPushTask().execute(getSparkFunPushUrl(BASE_URL_SPARK, API_KEY_PUBLIC_SPARK, API_KEY_PRIVATE_SPARK, sea_level_pressure));
            new JSONThingSpeakPushTask().execute(getThingSpeakPushUrl(BASE_URL_THINGSPEAK, API_KEY_PRIVATE_THINGSPEAK, sea_level_pressure));
        }

    }

    public class MyAlarmReceiver extends WakefulBroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
//        Intent background = new Intent(context, BackgroundService.class);

//        context.startService(background);
            Log.e("SimpleWakefulReceiver", "" + System.currentTimeMillis());
//            JSONSparkFunPushTask sparkTaskPush = new JSONSparkFunPushTask();
//            sparkTaskPush.execute(getSparkFunPushUrl(BASE_URL_SPARK, API_KEY_PUBLIC_SPARK, API_KEY_PRIVATE_SPARK, sea_level_pressure));
//            Log.i("SimpleWakefulReceiver", "" + System.currentTimeMillis());
        }
    }

    private class JSONSparkFunPushTask extends AsyncTask<String, Void, SparkFunPostStatus> {
        @Override
        protected SparkFunPostStatus doInBackground(String... params) {
            //data is received as a json string from the requested website
            String data = ((new HttpClientQuery()).getQueryResult(params[0]));
            SparkFunPostStatus status = new SparkFunPostStatus();
            try {
                //then data is parsed using a json parser into an elevation object
                status = JSONParser.getSparkFunPostStatus(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //returns the received elevation object
            return status;
        }

        @Override
        protected void onPostExecute(SparkFunPostStatus sparkFunPostStatus) {
            super.onPostExecute(sparkFunPostStatus);
            if (sparkFunPostStatus == null){
                Log.e("sparkPush", "failed to post the message!");
            } else {
                Log.e("sparkPush", "status=" + sparkFunPostStatus.isStatus() + " message=" + sparkFunPostStatus.getMessage());
            }
        }
    }

    private class JSONThingSpeakPushTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //data is received as a json string from the requested website
            Log.e("link", params[0]);
            String data = ((new HttpClientQuery()).getQueryResult(params[0]));
//            Log.e("data", data);
//            Integer status = null;
//            try {
//                status = Integer.parseInt(data);
//            } catch (NumberFormatException e){
//                e.printStackTrace();
//            }
            //returns the received elevation object
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("data", "thingspeak posted!");

//            if (result == null){
//                Log.e("thingspeak", "failed to post the message!");
//            } else {
//                Log.e("thingspeak", "success! Nr. " + result);
//            }
        }
    }

    private static int INTERVAL = 1000 * 60; //every minute
    Handler mHandler = new Handler();

    Runnable mHandlerTask = new Runnable()
    {
        @Override
        public void run() {
            if (checkConnectivity()){
//                new JSONSparkFunPushTask().execute(getSparkFunPushUrl(BASE_URL_SPARK, API_KEY_PUBLIC_SPARK, API_KEY_PRIVATE_SPARK, sea_level_pressure));
                new JSONThingSpeakPushTask().execute(getThingSpeakPushUrl(BASE_URL_THINGSPEAK, API_KEY_PRIVATE_THINGSPEAK, sea_level_pressure));
            }
            mHandler.postDelayed(mHandlerTask, INTERVAL);
        }
    };

    public void startRepeatedUpdates()
    {
        mHandlerTask.run();
    }

    public void stopRepeatingUpdates()
    {
        mHandler.removeCallbacks(mHandlerTask);
    }

    protected void setStatusBarTranslucent(boolean makeTranslucent) {
        if (makeTranslucent) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    private boolean checkConnectivity (){
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isConnected){
            Toast.makeText(this, "Could not access the Server! Check Connectivity!", Toast.LENGTH_SHORT).show();
        }
        return isConnected;
    }
}
