package com.monte.tangoapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import static android.hardware.SensorManager.getAltitude;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor pressureSensor;

    private TextView currentPressureText;
    private TextView currentAltitudeText;
    private TextView altitudeDifferenceText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialiseSensors();
        addViews();
    }

    public void initialiseSensors (){
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) == null){
            Toast.makeText(this, "Phone doesn't have Pressure sensor", Toast.LENGTH_SHORT).show();// Success! There's a pressure sensor.
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void addViews (){
        currentPressureText = (TextView) findViewById(R.id.currentPressureText);
        currentAltitudeText = (TextView) findViewById(R.id.currentAltitudeText);
        altitudeDifferenceText = (TextView) findViewById(R.id.altitudeDifferenceText);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_PRESSURE:
                millibars_of_pressure = event.values[0];
                Log.e("Pressure is", "" + millibars_of_pressure);
                float altitude = getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, millibars_of_pressure);
                currentAltitudeText.setText(String.format("%.3f m", altitude));
                currentPressureText.setText(String.format("%.3f mbar", millibars_of_pressure ));
                break;
        }
    }
    private float millibars_of_pressure;
    private float firstPointAltitude = 0.0f;
    private float secondPointAltitude= 0.0f;

    public void getPoints (View view){
        switch (view.getId()){
            case R.id.firstPointButton:
                firstPointAltitude = getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, millibars_of_pressure);
                altitudeDifferenceText.setText("0 m");
                Toast.makeText(this, "1st Point Collected!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.secondPointButton:
                secondPointAltitude = getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, millibars_of_pressure);
                Toast.makeText(this, "2st Point Collected!", Toast.LENGTH_SHORT).show();
                float diff = secondPointAltitude - firstPointAltitude;
                altitudeDifferenceText.setText(String.format("%.3f m", diff));
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
