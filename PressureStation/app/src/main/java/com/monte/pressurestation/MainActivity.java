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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor pressureSensor;
    private float millibars_of_pressure = 0;
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

            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, System.currentTimeMillis(), 60000, pendingIntent);
//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, 10000, pendingIntent);
        }

//        AlarmManager alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, YourAlarmReceiver.class),PendingIntent.FLAG_CANCEL_CURRENT);

// Use inexact repeating which is easier on battery (system can phase events and not wake at exact times)
//        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, System.currentTimeMillis(), 10000, pendingIntent);

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

    private String BASE_URL_SPARK_= "https://data.sparkfun.com/";
    private  String API_KEY_PUBLIC_SPARK = "***REMOVED***";
    private String API_KEY_PRIVATE_SPARK = "***REMOVED***";

    private String[] getSparkFunPushUrl (String baseAddress, String apiPublicKey, String apiPrivateKey, float pressure){
        long unixTime = System.currentTimeMillis() / 1000L;
        return new String[] {baseAddress + "input/" + apiPublicKey + ".json?private_key=" + apiPrivateKey +
                "&location=" + "edinburgh" + "&pressure=" + String.format("%.3f", pressure) + "&time=" + unixTime};
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
        //enable sensors when app is active

        sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected void onPause() {
        super.onPause();
        //disable sensors when app is in sleep
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_PRESSURE:  //if it was pressure, get the pressure value

                pressureFilter.add(event.values[0]);
                if (pressureFilter.size() > 5) {
                    pressureFilter.remove(0);
                }
                millibars_of_pressure = calculateAverage(pressureFilter);
        }
    }

    private List<Float> pressureFilter = new ArrayList<>();
    private float calculateAverage(List <Float> vals) {
        float sum = 0;
        if(!vals.isEmpty()) {
            for (Float mark : vals) {
                sum += mark;
            }
            return sum / vals.size();
        }
        return sum;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void pushDataButton (View v){
        JSONSparkFunPushTask sparkTaskPush = new JSONSparkFunPushTask();
        sparkTaskPush.execute(getSparkFunPushUrl(BASE_URL_SPARK_, API_KEY_PUBLIC_SPARK, API_KEY_PRIVATE_SPARK, millibars_of_pressure));
    }

    public class MyAlarmReceiver extends WakefulBroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
//        Intent background = new Intent(context, BackgroundService.class);

//        context.startService(background);
            Log.i("SimpleWakefulReceiver", "" + System.currentTimeMillis());
            JSONSparkFunPushTask sparkTaskPush = new JSONSparkFunPushTask();
            sparkTaskPush.execute(getSparkFunPushUrl(BASE_URL_SPARK_, API_KEY_PUBLIC_SPARK, API_KEY_PRIVATE_SPARK, millibars_of_pressure));
            Log.i("SimpleWakefulReceiver", "" + System.currentTimeMillis());
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
            Log.e("sparkPush", "status=" + sparkFunPostStatus.isStatus() + " message=" + sparkFunPostStatus.getMessage());
//            for (SparkFunWeather sparkFunWeather: sparkFunWeatherList) {
//                Log.e("spark", sparkFunWeather.getLocation() + " " + sparkFunWeather.getPressureGroundLevel() + " " + sparkFunWeather.getUnixTime());
//            }
        }
    }
}
