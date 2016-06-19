package com.monte.tangoapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.print.PrintHelper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
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

public class MainActivity extends AppCompatActivity implements SensorEventListener, ListView.OnItemLongClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final String base = "https://maps.googleapis.com/maps/api/elevation/json?locations=39.7391536,-104.9847034";
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private final int MY_PERMISSIONS_REQUEST_ACCESS_INTERNET = 2;
    private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3;
    private final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 4;

    private static String BASE_URL_OPEN_WEATHER_MAP = "http://api.openweathermap.org/data/2.5/weather?";
    private String API_KEY_OPEN_WEATHER_MAP = "***REMOVED***";

    private String BASE_URL_GOOGLE_ELEVATION = "https://maps.googleapis.com/maps/api/elevation/json?";
    private String API_KEY_GOOGLE_ELEVATION = "***REMOVED***";

    private String BASE_URL_FORECAST = "https://api.forecast.io/forecast/";
    private String API_KEY_FORECAST = "***REMOVED*** ";

    private double lat = 39.7391536;
    private double lon = -104.9847034;

    private SensorManager sensorManager;
    private Sensor pressureSensor;

    private TextView currentPressureText;
    private TextView currentAltitudeText;
    private TextView altitudeDifferenceText;
    private EditText currentLocation;
    private ArrayAdapter locationListAdapter;
//    private List locationList;

    //Location stuff
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String bestProvider;
    private GoogleApiClient mGoogleApiClient;

    private List altitudeList = new ArrayList();
    private List locationList = new ArrayList();
    private List pressureList = new ArrayList();

    private List displayTextList = new ArrayList();

    private LocationListAdapter customLocationListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialiseSensors();
        addViews();
        initialiseLocationProvider();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


    }

    private void initialiseLocationProvider() {
        //Allows an app to access precise location.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
//
//        // initialisation of location manager is created here
//        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        locationListener = new mylocationListener();
//
//        // tell the user if the GPS or Network is working or not
//        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
//                !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//            Toast.makeText(this, "GPS or Network is available", Toast.LENGTH_LONG).show();
//        }
//
//        // best provider here is to help users to find out the best provider of the useful providers
//        bestProvider = locationManager.getBestProvider(getcriteria(), true);
//
//        if (locationManager != null)
//            locationManager.requestLocationUpdates(bestProvider, 1000, 1, locationListener);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);


        if (mLastLocation != null) {
            lat = mLastLocation.getLatitude();
            lon = mLastLocation.getLongitude();

            Log.e ("Lat", mLastLocation.getLatitude()+ "");
            Log.e ("Long", mLastLocation.getLongitude()+ "");
            Log.e ("Altitude", mLastLocation.getAltitude()+ "");
            Toast.makeText(getApplicationContext(), "Lat= " + (mLastLocation.getLatitude()) +
                    " Long= " + (mLastLocation.getLongitude()), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class JSONTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... params) {
            Weather weather = new Weather();
            String data = ((new HttpClientQuery()).getQueryResult(params[0]));
            Log.e("Data=", data);
            /*
            try {
//                weather = JSONWeatherParser.getWeather(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            */
            return weather;

        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);

            if (weather == null)
                return;
//            local_sea_level_pressure = weather.getPressureSeaLevel();
//            local_temperature = weather.getTemperature();
//            local_relative_humidity = weather.getHumidity();
//            local_pressure = weather.getPressure();
//            unix_time = weather.getUnixTime();
//            Toast.makeText(getApplicationContext(), "Sea level pressure: " + local_sea_level_pressure
//                    + " hPa\nLocal pressure: " + local_pressure
//                    + " hPa\nTemperature: " + local_temperature
//                    + " K\nHumidity: " + local_relative_humidity
//                    +" %\nUnix Time: " + unix_time, Toast.LENGTH_SHORT).show();
//            Log.e("Edinburgh Pressure:", local_sea_level_pressure + "");
        }
    }

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

        locationListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,displayTextList);
        ListView locationListView = (ListView) findViewById(R.id.locationList);
        locationListView.setOnItemLongClickListener(this);
        locationListView.setAdapter(locationListAdapter);
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_PRESSURE:
                millibars_of_pressure = event.values[0];
                float altitude = getAltitude(local_sea_level_pressure, millibars_of_pressure);
                currentAltitudeText.setText(String.format("%.3f m", altitude));
                currentPressureText.setText(String.format("%.3f mbar", millibars_of_pressure ));
                break;
        }
    }
    private float millibars_of_pressure;
    private float firstPointAltitude = 0.0f;
    private float secondPointAltitude= 0.0f;
    private float local_sea_level_pressure = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;

    public void getPoints (View view){
        switch (view.getId()){
            case R.id.firstPointButton:
                firstPointAltitude = getAltitude(local_sea_level_pressure, millibars_of_pressure);
                altitudeDifferenceText.setText("0 m");
                Toast.makeText(this, "1st Point " + String.format("%.3f m", firstPointAltitude), Toast.LENGTH_SHORT).show();
                break;
            case R.id.secondPointButton:
                secondPointAltitude = getAltitude(local_sea_level_pressure, millibars_of_pressure);
                Toast.makeText(this, "2nd Point " + String.format("%.3f m", firstPointAltitude), Toast.LENGTH_SHORT).show();
                float diff = secondPointAltitude - firstPointAltitude;
                altitudeDifferenceText.setText(String.format("%.3f m", diff));
                break;
        }
    }

    private int elementIndex = 0;
    public void addLocation (View view){
        if (currentLocation.getText().toString().isEmpty()) {
            Toast.makeText(this, "Add location name", Toast.LENGTH_SHORT).show();
            return;
        }


        float altitude = getAltitude(local_sea_level_pressure, millibars_of_pressure);

        locationList.add(currentLocation.getText().toString());
        pressureList.add(String.format("%.3f", millibars_of_pressure));
        altitudeList.add(String.format("%.3f", altitude));

        elementIndex++;
        locationListAdapter.add(elementIndex + ". " + currentLocation.getText().toString()
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
                elementIndex = 0;
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

    private float local_temperature = 273.0f;
    private float local_relative_humidity = 90.0f;
    private String unix_time = new String();
    private float local_pressure = 0.0f;

    private void exportToFile (String folderName){
        // Here, thisActivity is the current activity

        String dataString = new String();
//        String sea_level_pressure = String.format("%.2f", local_sea_level_pressure);
        for (int i = 0; i < locationList.size(); i++){
            dataString += "\"" +  locationList.get(i) +"\",\"" + pressureList.get(i)
                    + "\",\"" + local_pressure + "\",\"" + local_sea_level_pressure
                    + "\",\"" + local_temperature + "\",\"" + local_relative_humidity
                    + "\",\"" + unix_time + "\",\"" + altitudeList.get(i) + "\"\n";
        }


        String columnString =   "\"Location\",\"Barometer results (hPa)\",\"Local pressure (hPa)\",\"Sea Level Pressure(hPa)\",\"Temperature (K)\",\"Humidity (%)\",\"Station updating Time (Unix)\",\"Altitude (m)\"";
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
                break;
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_INTERNET: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
//                    finish();
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
//                    finish();
                }
                break;
            }
        }
    }

    private String getOpenWeatherMapUrl (String baseAddress, String apiKey, double lat, double lon){
        return baseAddress + "lat=" + lat + "lon=" + lon + "&APPID=" + apiKey;
    }

    private String getGoogleElevationUrl (String baseAddress, String apiKey, double lat, double lon){
        return baseAddress + "locations=" + lat + "," + lon + "&key=" + apiKey;
    }

    private String getForecastUrl (String baseAddress, String apiKey, double lat, double lon){
        return baseAddress + "/" + apiKey + "/" + lat + "," + lon;
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
        if (id == R.id.action_refresh) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.INTERNET)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.INTERNET},
                        MY_PERMISSIONS_REQUEST_ACCESS_INTERNET);
            } else {
//                String city = "Edinburgh,UK";
                JSONTask openWeatherMapTask = new JSONTask();
                openWeatherMapTask.execute(getOpenWeatherMapUrl(BASE_URL_OPEN_WEATHER_MAP, API_KEY_OPEN_WEATHER_MAP, lat, lon));

                JSONTask googleElevationTask = new JSONTask();
                googleElevationTask.execute(getGoogleElevationUrl(BASE_URL_GOOGLE_ELEVATION, API_KEY_GOOGLE_ELEVATION, lat, lon));

                JSONTask forecastTask = new JSONTask();
                forecastTask.execute(getForecastUrl(BASE_URL_FORECAST, API_KEY_FORECAST, lat, lon));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
