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
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.monte.tangoapp.model.Elevation;
import com.monte.tangoapp.model.Weather;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.hardware.SensorManager.getAltitude;

/**
 * Create by Monte (Montvydas Klumbys) 2016/06.
 * Name of the Application is TangoApp.
 *
 * The written Application used as part of the Master's project.
 * The purpose of the project is to use barometer sensor to evaluate
 * the floor level/number in a multi-floor building. The application
 * additionally uses internet connection and GPS, which are widely
 * available. To add, the application works indoors-only.
 *
 * The Application constantly scans GPS location accuracy.
 * If accuracy drops below a certain threshold, application
 * notes that the user entered the building and thus starts
 * showing the correct floor. The primary use of the application
 * is to collect data for further analysis.
 *
 * To achieve this the application uses these APIs:
 *  -   Google Location Client
 *  -   Google Maps
 *  -   Forecast.io
 *  -   Google Elevation Service
 */

public class MainActivity extends AppCompatActivity implements SensorEventListener, ListView.OnItemLongClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, LocationListener {

    //constants used when requesting permissions
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private final int MY_PERMISSIONS_REQUEST_ACCESS_INTERNET = 2;
    private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3;
    private final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 4;

    //Different API base addresses and keys, which will be used to built the full request address
    //Open weather API info; no longer used, but given for informative purposes
    private static String BASE_URL_OPEN_WEATHER_MAP = "http://api.openweathermap.org/data/2.5/weather?";
    private String API_KEY_OPEN_WEATHER_MAP = "***REMOVED***";

    //Google elevation API info;
    private String BASE_URL_GOOGLE_ELEVATION = "https://maps.googleapis.com/maps/api/elevation/json?";
    private String API_KEY_GOOGLE_ELEVATION = "***REMOVED***";

    //Forecast.io API info;
    private String BASE_URL_FORECAST = "https://api.forecast.io/forecast/";
    private String API_KEY_FORECAST = "***REMOVED***";

    //latitude and longitude of the device; Initially set to the Main Library of the University of Edinburgh location
    //as the final test will be performed in there;
    private double lat = 55.942680;
    private double lon = -3.189038;

    // Define  sensor  manager and  the  pressure  sensor  objects
    private SensorManager sensorManager;
    private Sensor pressureSensor;
    //ambient temperature and humidity sensors do not exist in nexus 5, so are not used, however
    //example of how to use them is given in the comments
    //private Sensor ambientTemperatureSensor;
    //private Sensor relativeHumiditySensor;

    //Various UI elements for displaying useful information
    private TextView currentPressureText;       //Currently measured pressure from the barometer sensor
    private TextView currentAltitudeText;       //Calculated altitude using the barometer sensor and weather information
    private TextView altitudeDifferenceText;    //together with 1ST POINT & 2ND POINT Buttons used to show the altitude difference between two points in space
    private EditText currentLocation;           //Field for entering the location or the floor level
    private ArrayAdapter locationListAdapter;   //Adapter for storing and later displaying the collected locations in a listView
    private TextView currentGoogleAltitudeText; //Google altitude Text displays the requested Google Elevation
    private TextView currentFloorText;          //Shows the Current Floor number
    private TextView gpsAccuracyText;           //Displays the accuracy of the gps signal
    private GoogleMap mMap;                     //Google Maps fragment


    private GoogleApiClient mGoogleApiClient;   //Google client for requesting the location of the device

    private List altitudeList = new ArrayList();//List for storing collected altitudes
    private List locationList = new ArrayList();//List for storing collected {locations}/{actual floor levels}
    private List pressureList = new ArrayList();//List for storing collected pressure values for further evaluation
    private List displayTextList = new ArrayList();//List for later displaying the collected information


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);     //add xml
        updateValuesFromBundle(savedInstanceState); //update the saved state with previous values
        initialiseSensors();                        //acquire sensors
        addViews();                                 //acquire views
        initialiseLocationProvider();               //acquire location provider
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //screen always portrait

        //Google maps fragment is found and map is asked to be acquired
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    //The following used when the app is allowed to go to sleep to save the ground floor level offset
    private final String GOOGLE_OFFSET_KEY = "GOOGLE_OFFSET";
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //requires the key to specify which value is being saved
        outState.putFloat(GOOGLE_OFFSET_KEY, googleOffset);
        super.onSaveInstanceState(outState);
    }

    //The method has to be called in onCreate() to receive the saved ground floor value
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            //Check for specific key, which was predefined already
            if (savedInstanceState.keySet().contains(GOOGLE_OFFSET_KEY)) {
                googleOffset= savedInstanceState.getFloat(GOOGLE_OFFSET_KEY);
            }
        }
    }


    /**
     * Location provider - GoogleApiClient is created
     */
    private void initialiseLocationProvider() {
        //Allows an app to access precise location, thus ask for permissions for that
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        } else {
            //If permissions are already given
            //Create an instance of GoogleAPIClient
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //when connected to google location api, start locatoin updates
        startLocationUpdates();
    }

    /**
     * Location updates are being started with specific parameters such as update interval
     */
    protected void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();   //create location request object
        mLocationRequest.setInterval(10000);                        //specify request interval
        mLocationRequest.setFastestInterval(5000);                  //request location updates every 5 seconds at fastest
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);   //force to use GPS location for the best accuracy

        //check if the user gave the permission to access the device location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You did not allow the App to access location!", Toast.LENGTH_LONG).show();
            return;
        }

        //check again if the client was successfully created
        //and then request location to be updated
        if (mGoogleApiClient != null)
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    //method requests queries from different APIs; Call this method when the user enters the building and
    //and whenever he wants to get accurate floor level
    private void getQueryData() {
        //internet access is required, thus ask for this permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    MY_PERMISSIONS_REQUEST_ACCESS_INTERNET);
        } else {
            //open weather request is no longer needed
//                JSONWeatherTask openWeatherMapTask = new JSONWeatherTask();
//                openWeatherMapTask.execute(getOpenWeatherMapUrl(BASE_URL_OPEN_WEATHER_MAP, API_KEY_OPEN_WEATHER_MAP, lat, lon));

            //need this boolean value to tell the barometer to update it's value with a new pressure value.
            isPressureIndoorUpdated = false;

            //make a query to google elevation api to receive elevation at the ground level
            JSONElevationTask googleElevationTask = new JSONElevationTask();
            googleElevationTask.execute(getGoogleElevationUrl(BASE_URL_GOOGLE_ELEVATION, API_KEY_GOOGLE_ELEVATION, lat, lon));

            //make a query to Forecast.io to receive weather
            JSONWeatherTask forecastTask = new JSONWeatherTask();
            forecastTask.execute(getForecastUrl(BASE_URL_FORECAST, API_KEY_FORECAST, lat, lon));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    //When Google Maps are ready, the method is called
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //check for location permission again
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //If permissions are given, acquire google maps instance
        mMap = googleMap;
        //allow showing the current location with a blue dot
        mMap.setMyLocationEnabled(true);
    }

    private Location mLastAccurateLocation = null;  //Last detected location by the GPS
    private Location mCurrentLocation = null;       //Current Location of the device
    private final float ACCURATE_DISTANCE = 15.0f;   //Threshold for detecting if the device is indoors or outdoors
    private boolean isIndoors = false;              //flag for specifying if the user is indoors or outdoors
    private boolean firstMeasurement = true;        //flag for specifying if it is the first location scan
    private boolean useGoogleReference = true;      //flag to specify, which method to use
    @Override
    public void onLocationChanged(Location location) {
        //debug messages:
        //Check if the user is indoors
        Log.i("isIndoors", isIndoors + "");
        //print the accuracy of the GPS
        Log.i("new Accuracy", location.getAccuracy() + " m");
        gpsAccuracyText.setText(location.getAccuracy() + " m");

        //During the first measurement the user might already be indoors
        if (firstMeasurement){
            //no longer first measurement
            firstMeasurement = false;
            //update the last known location
            mLastAccurateLocation = location;
            useGoogleReference = true;
        }

        //if the accuracy drops below the specified threshold AND
        // current accuracy is worse than previous accuracy by a certain amount, means the user is indoors
        if (location.getAccuracy() > ACCURATE_DISTANCE){
            //if the user was previously not indoors
            if (!isIndoors){
                //change the state of the user to indoors
                Log.e("enteretd the room", "true");
                isIndoors = true;

                //the current location is now the last accurate location;
                mCurrentLocation = mLastAccurateLocation;

                //get new latitude and longitude
                lat = mCurrentLocation.getLatitude();
                lon = mCurrentLocation.getLongitude();
                //print latitude, longitude and accuracy
                Log.e("new Location", "lat=" + lat + "long=" + lon + " accuracy=" + location.getAccuracy() + " m");

                Toast.makeText(getApplicationContext(), "You are now indoors!\nLat= " + lat +
                        " Long= " + lon, Toast.LENGTH_LONG).show();
                //call queries to the web (e.g. forecast.io)
                getQueryData();
            }
        } else {    //the user is outdoors
            //set flag that user is outdoors
            isIndoors = false;
            //update the last known location
            mLastAccurateLocation = location;
            useGoogleReference = false;
        }
    }

    //Elevation object saves the information about the elevation and resolution
    private Elevation googleElevation = new Elevation();

    //A request is made in a form of async task to perform in background
    private class JSONElevationTask extends AsyncTask<String, Void, Elevation> {
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
            //the parsed elevation object is assigned to googleElevation object and
            //this elevation is being displayed on a textView
            googleElevation = elevation;
            String alt = String.format("%.2f m", elevation.getAltitude());
            currentGoogleAltitudeText.setText(alt);

            //A marker is put on a screen after a new location is received and camera is moved to that place
            LatLng point = new LatLng(lat, lon);
            mMap.addMarker(new MarkerOptions().position(point).title(String.format("Google Altitude= %.2f m", googleElevation.getAltitude())));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 18.5f));
            Log.e("google received", "google received");
            //set the flag stating that location is being updated, which will tell barometer sensor to update the ground floor level
            isGoogleUpdated = true;
        }
    }


    //weather object is holding the requested weather information from Forecast.io API
    private Weather forecastWeather = new Weather ();

    //A request is made in a form of async task to perform in background
    private class JSONWeatherTask extends AsyncTask<String, Void, Weather> {
        @Override
        protected Weather doInBackground(String... params) {
            //data is received as a json string from the requested website
            String data = ((new HttpClientQuery()).getQueryResult(params[0]));
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
            forecastWeather = weather;
            //update sea level pressure with a new value
            local_sea_level_pressure = weather.getPressureSeaLevel();
            //set flag for the barometer sensor to know that weather was already updated, he can calculate the offsets, etc.
            isWeatherUpdated = true;
            //display the information in form of a toast
            Toast.makeText(getApplicationContext(), "Sea level pressure: " + weather.getPressureSeaLevel()
                    + " hPa\nTemperature: " + weather.getTemperature()
                    + " K\nHumidity: " + weather.getHumidity()
                    +" %\nUnix Time: " + getTimeFromUnix( weather.getUnixTime()), Toast.LENGTH_SHORT).show();
        }
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
        //other sensors are also being requested in here, however will not be used as nexus 5 do not have them
//        ambientTemperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
//        if (sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) == null){
//            Toast.makeText(this, "The phone doesn't have ambient temperature sensor", Toast.LENGTH_SHORT).show();
//        }
//        relativeHumiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
//        if (sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY) == null){
//            Toast.makeText(this, "The phone doesn't have relative humidity sensor", Toast.LENGTH_SHORT).show();
//        }
    }

    /**
     * The UI elements are being defined in here from the xml file
     */
    private void addViews (){
        currentPressureText = (TextView) findViewById(R.id.currentPressureText);
        currentAltitudeText = (TextView) findViewById(R.id.currentAltitudeText);
        altitudeDifferenceText = (TextView) findViewById(R.id.altitudeDifferenceText);
        currentLocation = (EditText) findViewById(R.id.currentLocation);
        currentGoogleAltitudeText = (TextView) findViewById(R.id.currentGoogleAltitudeText);
        currentFloorText = (TextView) findViewById(R.id.currentFloorText);
        gpsAccuracyText = (TextView) findViewById(R.id.gpsAccuracyText);

        locationListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,displayTextList);
        ListView locationListView = (ListView) findViewById(R.id.locationList);
        locationListView.setOnItemLongClickListener(this);
        locationListView.setAdapter(locationListAdapter);
    }

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null)   //start location service by connecting to it
            mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //enable sensors when app is active
        sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
//        sensorManager.registerListener(this, ambientTemperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
//        sensorManager.registerListener(this, relativeHumiditySensor, SensorManager.SENSOR_DELAY_NORMAL);
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();     //also start location updates
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //disable sensors when app is in sleep
        sensorManager.unregisterListener(this);
        //also stop updating the location of the device to save the battery
        stopLocationUpdates();
    }

    //the method removes location updates
    protected void stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();  //then app stops, disconnect from google api client
        super.onStop();
    }

    private float altitude = 0.0f;                      //altitude of the device value
    private boolean isPressureIndoorUpdated = false;    //flag telling if the pressure from barometer was updated
    private boolean isGoogleUpdated = false;            //flag telling if the google elevation api was updated
    private boolean isWeatherUpdated = false;           //flag telling if the weather from forecast.io api was updated
    private float googleOffset = 16.65f;                //offset between the google ground altitude and the barometer ground altitude
    private float groundLevelAltitude = 16.65f;         //calculated ground level altitude
    @Override
    public void onSensorChanged(SensorEvent event) {
        //check which sensor was updated
        switch (event.sensor.getType()){
            case Sensor.TYPE_PRESSURE:  //if it was pressure, get the pressure value
                millibars_of_pressure = event.values[0];
                //calculate the altitude using the provided android static method
                altitude = getAltitude(local_sea_level_pressure, millibars_of_pressure);

                //Update UI elements with new altitude and pressure values
                currentAltitudeText.setText(String.format("%.3f m", altitude));
                currentPressureText.setText(String.format("%.3f mbar", millibars_of_pressure));

                //call method for calculating the floor level
                currentFloorText.setText(evaluateFloorNumber()+"");

                //calculates the offset value
                //All of the mentioned conditions must be when and then the ground floor level can be updated
                if (isIndoors && !isPressureIndoorUpdated && isGoogleUpdated && isWeatherUpdated){
                    Log.e("pressure updated", "google altitude=" + googleElevation.getAltitude() + " altitude=" + altitude);
                    if (useGoogleReference) {
                        groundLevelAltitude = googleElevation.getAltitude() - googleOffset;
                    } else {
                        groundLevelAltitude = altitude;
                    }
                    Toast.makeText(getApplicationContext(), String.format("Google Offset = %.2f m", googleOffset), Toast.LENGTH_LONG).show();
                    //Set flags to default states
                    useGoogleReference = true;
                    isPressureIndoorUpdated = true;
                    isGoogleUpdated = false;
                    isWeatherUpdated = false;
                }
                break;
            //Other sensors are not accessed
//            case Sensor.TYPE_AMBIENT_TEMPERATURE:
//                Log.e("ambient temperature", event.values[0] + " ˚C");
//                break;
//            case Sensor.TYPE_RELATIVE_HUMIDITY:
//                Log.e("relative humidity", event.values[0] + " %");
//                break;
        }
    }

    /**
     * Algorithm for calculating the floor number
     * @return floorNumber;
     */
    private int evaluateFloorNumber (){
        //Calculate the difference between the altitude and the groundLevelAltitude
        double dH = altitude - groundLevelAltitude;
        //Algorithm doesn't handle well values around ground floor, thus need some thresholds
        if (dH < 0.5 && dH > -0.5)
            dH = 0.0;
        //From the difference evaluate the floor number
        double floorNr = (Math.floor(dH / 3) + Math.ceil(dH / 4)) / 2;
        Log.e("values", "dH=" + dH +  " googleOffset=" + googleOffset + " altitude=" + altitude + " elevation=" + googleElevation.getAltitude());
        Log.e("floor", "Nr. " + (Math.floor(dH / 3) + Math.ceil(dH / 4)) / 2);
        if (floorNr < 0)
            return (int) Math.floor(floorNr);
        else if (floorNr > 0)
            return (int) Math.ceil(floorNr);
        else return 0;
    }

    private float millibars_of_pressure = 1013.25f; //the measured pressure from barometer sensor
    private float firstPointAltitude = 0.0f;        //1st & 2nd points for altitude difference calculations
    private float secondPointAltitude= 0.0f;
    private float local_sea_level_pressure = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;//sea level pressure

    /**
     * A button for calculating the altitude difference between two points.
     * Firstly press button 1ST POINT, then 2NS POINT and the altitude difference will be shown
     * @param view
     */
    public void getPoints (View view){
        switch (view.getId()){
            case R.id.firstPointButton:
                //use sea level pressure and pressure at the location to calculate the altitude
                firstPointAltitude = getAltitude(local_sea_level_pressure, millibars_of_pressure);
                //reset the text back to 0 m
                altitudeDifferenceText.setText("0 m");
                //show the altitude of the first point in a toast form
                Toast.makeText(this, "1st Point " + String.format("%.3f m", firstPointAltitude), Toast.LENGTH_SHORT).show();
                break;
            case R.id.secondPointButton:
                //use sea level pressure and pressure at the location to calculate the altitude
                secondPointAltitude = getAltitude(local_sea_level_pressure, millibars_of_pressure);
                //update the text with the difference between those two points
                float diff = secondPointAltitude - firstPointAltitude;
                altitudeDifferenceText.setText(String.format("%.3f m", diff));
                //show the altitude of the first point in a toast form
                Toast.makeText(this, "2nd Point " + String.format("%.3f m", secondPointAltitude), Toast.LENGTH_SHORT).show();
                break;
        }
    }
    private int elementIndex = 0;

    /**
     * A button for adding a data point;
     * Firstly enter the name of the location (usually floor level), then press
     * add location button and the locatoin will be saved in a list
     * @param view
     */
    public void addLocation (View view){
        //check for the name to not be empty
        if (currentLocation.getText().toString().isEmpty()) {
            Toast.makeText(this, "Add location name", Toast.LENGTH_SHORT).show();
            return;
        }

        //add the required data in the 4 provided lists
        locationList.add(currentLocation.getText().toString());
        pressureList.add(String.format("%.3f", millibars_of_pressure));
        altitudeList.add(String.format("%.3f", altitude));

        elementIndex++;
        locationListAdapter.add(elementIndex + ". " + currentLocation.getText().toString()
                + String.format("  %.3f mbar ", millibars_of_pressure)
                + String.format(" %.3f m", altitude));
        //notify the listView with the changes in the list
        locationListAdapter.notifyDataSetChanged();
        //clear the text field
        currentLocation.getText().clear();
    }

    /**
     * A button for deleting all of the collected data within the app
     * Simply press the button and it will ask if you want to delete everything.
     * @param view
     */
    public void removeAllLocations (View view){
        //check that there is actually anything to be deleted
        if (locationListAdapter.isEmpty()) {
            Toast.makeText(this, "Nothing to Delete", Toast.LENGTH_SHORT).show();
            return;
        }

        //create a dialog box to ask if you actually want to delete everything you have
        AlertDialog.Builder adb=new AlertDialog.Builder(MainActivity.this);
        adb.setTitle("Delete All Locations?");
        adb.setNegativeButton("Cancel", null);
        adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //deleting everything means clearing all of the lists
                locationList.clear();
                pressureList.clear();
                altitudeList.clear();
                displayTextList.clear();
                //adapter also has to be cleared
                locationListAdapter.clear();
                //and listView must be updated
                locationListAdapter.notifyDataSetChanged();
                //also reset the index number
                elementIndex = 0;
            }});
        adb.show();

    }

    /**
     * A button for exporting the collected data to a CSV file
     * @param view
     */
    public void exportLocations (View view){
        //check that there is something to export
        if (locationListAdapter.isEmpty()) {
            Toast.makeText(this, "Nothing to Export", Toast.LENGTH_SHORT).show();
            return;
        }

        //ask for permissions to access files on the phone
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            //create a dialog box with a text field to enter the folder name for storing data
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter Folder Name");

            // Set up the input text field
            final EditText folderLocation = new EditText(this);
            // Specify the type of input expected
            folderLocation.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(folderLocation);
            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //calls a method for storing files
                    exportToFile (folderLocation.getText().toString());
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //else does nothing
                    dialog.cancel();
                }
            });

            builder.show();
        }
    }


    /**
     * Exporting the data to a CSV file for further analysis.
     * @param folderName
     */
    private void exportToFile (String folderName){
        String dataString = new String();
//        String sea_level_pressure = String.format("%.2f", local_sea_level_pressure);
        for (int i = 0; i < locationList.size(); i++){
            dataString += "\"" +  locationList.get(i) +"\",\"" + pressureList.get(i)
                    + "\",\"" + forecastWeather.getPressureSeaLevel()
                    + "\",\"" + forecastWeather.getTemperature() + "\",\"" + forecastWeather.getHumidity()
                    + "\",\"" + getTimeFromUnix( forecastWeather.getUnixTime() ) + "\",\""+ googleElevation.getAltitude() + "\",\"" + altitudeList.get(i) + "\"\n";
        }

        String columnString =   "\"Location\",\"Barometer results (hPa)\",\"Sea Level Pressure(hPa)\",\"Temperature (K)\",\"Humidity (%)\",\"Station updating Time (Unix)\",\"Google Altitude (m)\",\"Altitude (m)\"";
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

        //Files will be saved in 2 places; One is /TangoApp/allData.csv
        //where ALL of the data will be saved without separating it into folders
        //This will be easier to analyse
        if (root.canWrite()){
            File dir    =   new File (root.getAbsolutePath() + "/TangoApp/" + folderName + "/");
            dir.mkdirs();

            file   =   new File(dir, formattedDate + ".csv");
            File allDataFile = new File (root.getAbsolutePath() + "/TangoApp/allData.csv");

            boolean fileExists = true;
            if (!allDataFile.exists()){
                fileExists = false;
            }

            try {
                FileWriter fw = new FileWriter(allDataFile, true);

                if (!fileExists) {
                    fw.write(columnString + "\n");
                }

                fw.write(dataString);
                fw.flush();
                fw.close();
                Toast.makeText(this, "Stored in /TangoApp/allData.csv", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Another is the separated in different folders, different files, so better,
            //when need nicely separated data
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
    //nothing needs to be done in here
    }

    //By making a long click on the list item one can delete it
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        //create an alert box to ask if you actually want to delete the collected data
        AlertDialog.Builder adb=new AlertDialog.Builder(MainActivity.this);
        adb.setTitle("Delete?");
        adb.setMessage("Are you sure you want to delete \"" + displayTextList.get(position) + "\"?");
        final int positionToRemove = position;
        adb.setNegativeButton("Cancel", null);
        adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //To delete one data point, means to remove the item from 4 lists in total
                locationList.remove(positionToRemove);
                pressureList.remove(positionToRemove);
                altitudeList.remove(positionToRemove);
                displayTextList.remove(positionToRemove);
                //update the listView with changes
                locationListAdapter.notifyDataSetChanged();
            }});
        adb.show();
        return false;
    }

    //Permission responses are handled in here;
    //Nothing was actually disabled, but the code automatically checks for permissions when running,
    // cause the app is still in debug mode
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

    /**
     * Constructs a link to make a query to OpenWeather API.
     * Queries require different ways of specifying the request link
     * in the form of (base address + api_key + options & flags)
     * @param baseAddress
     * @param apiKey
     * @param lat
     * @param lon
     * @return
     */
    private String [] getOpenWeatherMapUrl (String baseAddress, String apiKey, double lat, double lon){
        return new String[]{baseAddress + "lat=" + lat + "&lon=" + lon + "&APPID=" + apiKey};

    }

    /**
     * Constructs a link to make a query to Google Elevation API
     * @param baseAddress
     * @param apiKey
     * @param lat
     * @param lon
     * @return
     */
    private String[] getGoogleElevationUrl (String baseAddress, String apiKey, double lat, double lon){
        return new String[] {baseAddress + "locations=" + lat + "," + lon + "&key=" + apiKey};
    }

    /**
     * Constructs a link to make a query to Forecast.io API
     * @param baseAddress
     * @param apiKey
     * @param lat
     * @param lon
     * @return
     */
    private String [] getForecastUrl (String baseAddress, String apiKey, double lat, double lon){
        //Forecast.io additionally can specify flags to request less information
        return new String[] {baseAddress + apiKey + "/" + lat + "," + lon + "?units=si&exclude=minutely,hourly,daily,alerts,flags"};
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //compare the pressed option item id
        switch (item.getItemId()){
            case R.id.action_refresh://makes a query to google servers and forecast.io to return elevation and weather
                getQueryData();
                break;
            case R.id.action_calibrate: //calibrates google altitude offset and forces the application to use it
                //firstly get the offset
                googleOffset = googleElevation.getAltitude() - altitude;
                //then using the offset calculate the ground level altitude, used for algorithm
                groundLevelAltitude = googleElevation.getAltitude() - googleOffset;
                //Force the application to use Google Elevation as a ground reference
                useGoogleReference = true;
                //Make a toast to tell the use the new offset
                Toast.makeText(getApplicationContext(), String.format("Google Offset = %.2f m", googleOffset), Toast.LENGTH_LONG).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //extracts time from unix time
    private String getTimeFromUnix (long timeStamp){
        //create a Date object from Unix time
        Date date = new java.util.Date(timeStamp*1000);
        //format the Date according to the specified format
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // the format of your date
        //return the formatted result
        return formatter.format(date);
    }
}