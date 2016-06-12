package com.monte.tangoapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.monte.tangoapp.model.Weather;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.hardware.SensorManager.getAltitude;

//*** need to request permissions to access the internet

public class MainActivity extends AppCompatActivity implements SensorEventListener, ListView.OnItemLongClickListener{
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private final int MY_PERMISSIONS_REQUEST_ACCESS_INTERNET = 2;

    private SensorManager sensorManager;
    private Sensor pressureSensor;

    private TextView currentPressureText;
    private TextView currentAltitudeText;
    private TextView altitudeDifferenceText;
    private EditText currentLocation;
    private ArrayAdapter locationListAdapter;
//    private List locationList;

    List altitudeList = new ArrayList();
    List locationList = new ArrayList();
    List pressureList = new ArrayList();

    List displayTextList = new ArrayList();

    private LocationListAdapter customLocationListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialiseSensors();
        addViews();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    MY_PERMISSIONS_REQUEST_ACCESS_INTERNET);
        }

        String city = "Edinburgh,UK";
        JSONWeatherTask task = new JSONWeatherTask();
        task.execute(new String[]{city});
    }

    private class JSONWeatherTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... params) {
            Weather weather = new Weather();
            String data = ((new WeatherHttpClient()).getWeatherData(params[0]));
            Log.e("Data=", data);
            try {
                weather = JSONWeatherParser.getWeather(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return weather;
        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);
            national_pressure_mbar = weather.getPressure();
            Toast.makeText(getApplicationContext(), "Pressure in Edinburgh at sea level is " + national_pressure_mbar + " mbar", Toast.LENGTH_LONG).show();
            Log.e("Edinburgh Pressure:", national_pressure_mbar + "");
        }
    }

    private float national_pressure_mbar = 0;
    public void initialiseSensors (){
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) == null){
            Toast.makeText(this, "Phone doesn't have Pressure sensor", Toast.LENGTH_SHORT).show();// Success! There's a pressure sensor.
            try {
                Thread.sleep(1000);
                finish();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void addViews (){
        currentPressureText = (TextView) findViewById(R.id.currentPressureText);
        currentAltitudeText = (TextView) findViewById(R.id.currentAltitudeText);
        altitudeDifferenceText = (TextView) findViewById(R.id.altitudeDifferenceText);
        currentLocation = (EditText) findViewById(R.id.currentLocation);

//        customLocationListAdapter = new LocationListAdapter(this, locationList, pressureList, altitudeList);

        locationListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,displayTextList);
        ListView locationListView = (ListView) findViewById(R.id.locationList);
        locationListView.setOnItemLongClickListener(this);
//        locationListView.setAdapter(customLocationListAdapter);
        locationListView.setAdapter(locationListAdapter);
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
//                Log.e("Pressure is", "" + millibars_of_pressure);
                float altitude = getAltitude(national_pressure_mbar, millibars_of_pressure);
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
                Toast.makeText(this, "1st Point " + String.format("%.3f m", firstPointAltitude), Toast.LENGTH_SHORT).show();
                break;
            case R.id.secondPointButton:
                secondPointAltitude = getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, millibars_of_pressure);
                Toast.makeText(this, "2nd Point " + String.format("%.3f m", firstPointAltitude), Toast.LENGTH_SHORT).show();
                float diff = secondPointAltitude - firstPointAltitude;
                altitudeDifferenceText.setText(String.format("%.3f m", diff));
                break;
        }
    }

    public void addLocation (View view){
        if (currentLocation.getText().toString().isEmpty()) {
            Toast.makeText(this, "Add location name", Toast.LENGTH_SHORT).show();
            return;
        }


        float altitude = getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, millibars_of_pressure);
//        customLocationListAdapter.add(currentLocation.getText().toString(),
//                String.format(" %.3f m", millibars_of_pressure),
//                String.format(" %.3f m", altitude));
//        customLocationListAdapter.notifyDataSetChanged();

        locationList.add(currentLocation.getText().toString());
        pressureList.add(String.format("%.3f", millibars_of_pressure));
        altitudeList.add(String.format("%.3f", altitude));

        locationListAdapter.add(currentLocation.getText().toString()
                + String.format("  %.3f mbar ", millibars_of_pressure)
                + String.format(" %.3f m", altitude));
        locationListAdapter.notifyDataSetChanged();

        currentLocation.getText().clear();
    }

    public void removeAllLocations (View view){
        if (locationListAdapter.isEmpty()) {
            Toast.makeText(this, "Nothing to Delete", Toast.LENGTH_SHORT).show();
            return;
        }


        AlertDialog.Builder adb=new AlertDialog.Builder(MainActivity.this);
        adb.setTitle("Delete All Locations?");
        adb.setNegativeButton("Cancel", null);
        adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                locationList.clear();
                pressureList.clear();
                altitudeList.clear();
                displayTextList.clear();
                locationListAdapter.clear();
                locationListAdapter.notifyDataSetChanged();
            }});
        adb.show();

    }

    public void exportLocations (View view){
        if (locationListAdapter.isEmpty()) {
            Toast.makeText(this, "Nothing to Export", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?

//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//            } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Folder Name");

        // Set up the input
        final EditText folderLocation = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        folderLocation.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(folderLocation);


// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                exportToFile (folderLocation.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
//                dialog.dismiss();
            }
        });

        builder.show();

    }

    private void exportToFile (String folderName){
        // Here, thisActivity is the current activity

        String dataString = new String();
        for (int i = 0; i < locationList.size(); i++){
            dataString += "\"" +  locationList.get(i) +"\",\"" + pressureList.get(i) + "\",\"" + altitudeList.get(i) + "\"\n";
        }


        String columnString =   "\"Location\",\"Pressure (mbar)\",\"Altitude (m)\"";
        String combinedString = columnString + "\n" + dataString;

        Log.e("locations", combinedString);
        Log.e("FolderName", folderName);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy_HH:mm");
        String formattedDate = df.format(c.getTime());

        Log.e("data", formattedDate);

        File file   = null;
        File root   = Environment.getExternalStorageDirectory();

        System.out.println(root);


        if (root.canWrite()){
            File dir    =   new File (root.getAbsolutePath() + "/TangoApp/" + folderName + "/");
            dir.mkdirs();

            file   =   new File(dir, formattedDate + ".csv");
            FileOutputStream out   =   null;
            try {
                out = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                out.write(combinedString.getBytes());
                Toast.makeText(this, "Stored in /TangoApp/" + folderName + "/" + formattedDate + ".csv", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        AlertDialog.Builder adb=new AlertDialog.Builder(MainActivity.this);
        adb.setTitle("Delete?");
        adb.setMessage("Are you sure you want to delete \"" + displayTextList.get(position) + "\"?");
        final int positionToRemove = position;
        adb.setNegativeButton("Cancel", null);
        adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                locationList.remove(positionToRemove);
                pressureList.remove(positionToRemove);
                altitudeList.remove(positionToRemove);

                displayTextList.remove(positionToRemove);
                locationListAdapter.notifyDataSetChanged();
            }});
        adb.show();
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_INTERNET: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                return;
            }
        }
    }
}
