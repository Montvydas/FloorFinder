package com.monte.tangoapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.monte.tangoapp.model.Elevation;
import com.monte.tangoapp.model.SparkFunWeather;
import com.monte.tangoapp.model.Weather;
import com.monte.tangoapp.tasks.AddressTask;
import com.monte.tangoapp.tasks.AddressTaskListener;
import com.monte.tangoapp.tasks.ElevationTaskListener;
import com.monte.tangoapp.tasks.JSONElevationTask;
import com.monte.tangoapp.tasks.JSONSparkFunPullTask;
import com.monte.tangoapp.tasks.JSONWeatherTask;
import com.monte.tangoapp.tasks.LocationTaskListener;
import com.monte.tangoapp.tasks.MyProgressRunner;
import com.monte.tangoapp.tasks.SparkFunTaskListener;
import com.monte.tangoapp.tasks.WeatherTaskListener;

import java.util.List;
import java.util.Locale;

/**
 * Created by monte on 04/01/2017.
 */
public class UserActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener,
        ElevationTaskListener, LocationTaskListener, SparkFunTaskListener, WeatherTaskListener, AddressTaskListener {
    //latitude and longitude of the device; Initially set to the Main Library of the University of Edinburgh location
    //as the final test will be performed in there;
    private double lat = 55.942680;
    private double lon = -3.189038;

    // Define  sensor  manager and  the  pressure  sensor  objects
    private SensorManager sensorManager;
    private Sensor pressureSensor;

    private TextView floorLevelText;
    private ProgressBar progressBar;
    private int actualFloorLevel = 0;
    private MyProgressRunner runnerTask = null;

    private TextView countryText;
    private TextView cityText;

    //Location Stuff
    private LocationUpdater mLocationUpdater;
    private FloorOffsets myCountryOffset;

    private boolean pressureReferenceReady = false;
    private boolean groundLevelAltitudeReady = false;
    private boolean addressReady = false;

    private double userLevelAltitude = 0.0;
    private double referencePressure = 1013.0;
    private Location EDINBURGH_STATION = new Location("edinburgh_station_coordinates");

    private ImageView accuracyImage;

    private int pStatus = 0;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarTranslucent(true);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#250054'>" + getString(R.string.app_name) + "</font>"));

        setContentView(R.layout.activity_user);

        Constants.OFFSET_TO_GOOGLE = getGoogleOffset();

        EDINBURGH_STATION.setLatitude(55.939036);
        EDINBURGH_STATION.setLongitude(-3.187160);

        initialiseLocationUpdater();
        initialiseSensors();
        myCountryOffset = new FloorOffsets();

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //screen always portrait

        countryText = (TextView) findViewById(R.id.text_country);
        cityText = (TextView) findViewById(R.id.text_city);
        floorLevelText = (TextView) findViewById(R.id.text_floor_level);
        floorLevelText.setText("--");
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setOnClickListener(this);
        accuracyImage = (ImageView) findViewById(R.id.image_accuracy);
        accuracyImage.setOnClickListener(this);
    }

    private boolean keepUpdating = false;
    private boolean isSavingGoogleOffset = false;
    private final float filterSmooth = 0.2f;

    private float currPressure = 1013.25f;
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_PRESSURE:  //if it was pressure, get the pressure value
                currPressure = event.values[0]*filterSmooth + currPressure*(1-filterSmooth);

                if (pressureReferenceReady && groundLevelAltitudeReady && addressReady){
                    currPressure = event.values[0];
                    pressureReferenceReady = false;
                    groundLevelAltitudeReady = false;
                    addressReady = false;

                    if (isSavingGoogleOffset){
                        float altitude = SensorManager.getAltitude((float) referencePressure, currPressure);
                        float newOffset = altitude - (float) groundLevelAltitude;
                        float combinedOffset = offsetFilterWeight*newOffset + (1-offsetFilterWeight)*Constants.OFFSET_TO_GOOGLE;

                        setNewGoogleOffset(combinedOffset);
                        Constants.OFFSET_TO_GOOGLE = getGoogleOffset();
                        isSavingGoogleOffset = false;
                    }

//                    Log.e("ref type", actualReferenceUsed + " ID");

                    //stop spinning
                    if (runnerTask != null) {
                        pStatus = runnerTask.getStatus();
                        runnerTask.interrupt();
                        try {
                            runnerTask.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runnerTask = null;
                    }
                    keepUpdating = true;

                    if (actualReferenceUsed == 0){
                        accuracyImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_accuracy_1));
                    } else if (actualReferenceUsed == 1){
                        accuracyImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_accuracy_2));
                    } else if (actualReferenceUsed == 2){
                        accuracyImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_accuracy_3));
                    } else {
                        accuracyImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_accuracy_0));
                    }
                }

                if (keepUpdating){
                    userLevelAltitude = SensorManager.getAltitude((float) referencePressure, currPressure);

                    actualFloorLevel = getFloorNumber(userLevelAltitude, groundLevelAltitude, Constants.OFFSET_TO_GOOGLE);
                    countryText.setText(myCountryOffset.getAddress().getCountryName());
                    cityText.setText(myCountryOffset.getAddress().getLocality());
                    if (Constants.IS_AUTO_BUILDING_TYPE) {
                        floorLevelText.setText(String.valueOf(myCountryOffset.getCountryFloor(actualFloorLevel)));
                    } else {
                        floorLevelText.setText(String.valueOf(actualFloorLevel));
                    }
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.progress_bar:
                startScanForData();
                break;
            case R.id.image_accuracy:
                AlertDialog.Builder adb=new AlertDialog.Builder(UserActivity.this);

                if (actualReferenceUsed == 0){
                    adb.setTitle(String.format("Reference - Default Constant Sea Pressure"));
                } else if (actualReferenceUsed == 1){
                    adb.setTitle(String.format("Reference - Forecast.io Station"));
                } else if (actualReferenceUsed == 2){
                    adb.setTitle(String.format("Reference - Custom Edinburgh Station"));
                } else {
                    adb.setTitle(String.format("Reference - Default Constant Sea Pressure"));
                }

                adb.setPositiveButton("Ok", null);
                adb.show();
                break;
        }
    }

    public void startScanForData (){
        if (!checkConnectivity()){
            isSavingGoogleOffset = false;
            return;
        }

        if (checkGPS(this)){
            mLocationUpdater.startLocationUpdates();
            accuracyImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_accuracy_0));

            if (runnerTask == null){
                runnerTask = new MyProgressRunner(pStatus, progressBar);
                runnerTask.start();
            }
        } else {
            isSavingGoogleOffset = false;
            Toast.makeText(this, "You need to enable GPS for this application to work!", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean checkGPS (Context mContext){
        LocationManager locationManager = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
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

    @Override
    public void onWeatherUpdated(Weather weather){
        if (weather == null){
            Toast.makeText(this, "You used up all available daily 1000 requests! Default values used!", Toast.LENGTH_SHORT).show();
            referencePressure = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;
            actualReferenceUsed = 0;
        } else {
            referencePressure = weather.getPressureSeaLevel();
            actualReferenceUsed = 1;
        }
        pressureReferenceReady = true;
    }

    @Override
    public void onSparkFunUpdated (List<SparkFunWeather> sparkFunWeatherList){
        if (sparkFunWeatherList == null){
            if (checkConnectivity()) {
                Toast.makeText(this, "Error while accessing SparkFun servers, trying again...", Toast.LENGTH_SHORT).show();
                new JSONSparkFunPullTask(this).execute(Constants.getSparkFunPullUrl(Constants.BASE_URL_SPARK_, Constants.API_KEY_PUBLIC_SPARK));
            } else {
                Toast.makeText(this, "No internet connectivity...", Toast.LENGTH_SHORT).show();
                referencePressure = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;
                actualReferenceUsed = 0;
                pressureReferenceReady = true;

            }
        } else {
            if (sparkFunWeatherList.size() > 0) {
                referencePressure = sparkFunWeatherList.get(0).getPressureGroundLevel();
                actualReferenceUsed = 2;
                pressureReferenceReady = true;
            } else {
                //make a query to Forecast.io to receive weather
                Toast.makeText(this, "No data in the past 10min from SparkFun! Forecast.io Sea Level used!", Toast.LENGTH_SHORT).show();
                JSONWeatherTask forecastTask = new JSONWeatherTask(this);
                forecastTask.execute(Constants.getForecastUrl(Constants.BASE_URL_FORECAST,
                        Constants.API_KEY_FORECAST, lat, lon));
            }
        }
    }

    private double groundLevelAltitude = 0.0;
    @Override
    public void onElevationUpdated (Elevation elevation){
        if (elevation == null){
            Toast.makeText(this, "You used up all available daily 1000 requests!", Toast.LENGTH_SHORT).show();
            groundLevelAltitude = 0;
        } else {
            groundLevelAltitude = elevation.getAltitude();
        }

        groundLevelAltitudeReady = true;
    }

    @Override
    public void onAddressUpdated(Address address) {
        if (address == null){
            Address newAddress = new Address(new Locale("--"));
            newAddress.setLocality("--");
            newAddress.setCountryName("--");
            myCountryOffset.setAddress(newAddress);
            myCountryOffset.setFloorOffsetFromCountry("--");
            myCountryOffset.setNumberingConvention("--");
            addressReady = true;
        } else {
            String countryCode = address.getCountryCode();
            if (countryCode == null){
                countryCode = "UK";
            }
            myCountryOffset.setAddress(address);
            myCountryOffset.setFloorOffsetFromCountry(countryCode);
            myCountryOffset.setNumberingConvention(countryCode);
            addressReady = true;
        }
    }

    private int actualReferenceUsed = 1;

    @Override
    public void onLocationUpdated (Location location){
        mLocationUpdater.stopLocationUpdates();

        lat = location.getLatitude();
        lon = location.getLongitude();

        if (Constants.REFERENCE_TYPE == 0){ //auto
            if (location.distanceTo(EDINBURGH_STATION) > Constants.EDINBURGH_STATION_OFFSET){
                //make a query to Forecast.io to receive weather
                new JSONWeatherTask(this).execute(Constants.getForecastUrl(Constants.BASE_URL_FORECAST, Constants.API_KEY_FORECAST, lat, lon));
            } else {
                //use custom edinburgh station
                new JSONSparkFunPullTask(this).execute(Constants.getSparkFunPullUrl(Constants.BASE_URL_SPARK_, Constants.API_KEY_PUBLIC_SPARK));
            }
        } else if (Constants.REFERENCE_TYPE == 1){  //edinburgh station
            new JSONSparkFunPullTask(this).execute(Constants.getSparkFunPullUrl(Constants.BASE_URL_SPARK_, Constants.API_KEY_PUBLIC_SPARK));
        } else if (Constants.REFERENCE_TYPE == 2){  //forecast
            new JSONWeatherTask(this).execute(Constants.getForecastUrl(Constants.BASE_URL_FORECAST, Constants.API_KEY_FORECAST, lat, lon));
        } else if (Constants.REFERENCE_TYPE == 3) {  //default sea pressure
            referencePressure = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;
            actualReferenceUsed = 0;
            pressureReferenceReady = true;
        }

        new JSONElevationTask(this).execute(Constants.getGoogleElevationUrl(Constants.BASE_URL_GOOGLE_ELEVATION,
                Constants.API_KEY_GOOGLE_ELEVATION, lat, lon));

        new AddressTask(this, this).execute(new Double[]{lat, lon});
    }

    @Override
    public void onConnected() {
        startScanForData();
    }

    private int getFloorNumber (double altitude, double groundLevelAltitude, double offset){
        double dH = altitude - groundLevelAltitude - offset;
        return (int) Math.round(0.3013 * dH - 0.1071);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user, menu);
        return true;
    }

    private float offsetFilterWeight = 0.0f;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //compare the pressed option item id
        switch (item.getItemId()){
            case R.id.action_settings://makes a query to google servers and forecast.io to return elevation and weather
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_calibrate:
                AlertDialog.Builder adb=new AlertDialog.Builder(UserActivity.this);
                adb.setTitle("Calibrate Readings");
                adb.setMessage("Stay on the ground floor and keep the phone in a stable position. Press 'NEW' to reset previous calibration history and 'UPDATE' to update history with new value.");
                adb.setNegativeButton("Cancel", null);
                adb.setNeutralButton("Update" , new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //deleting everything means clearing all of the lists
                        isSavingGoogleOffset = true;
                        setOffsetCheck(true);
                        startScanForData();
                        offsetFilterWeight = 0.1f;
                    }
                });
                adb.setPositiveButton("New", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //deleting everything means clearing all of the lists
                        isSavingGoogleOffset = true;
                        setOffsetCheck(true);
                        startScanForData();
                        offsetFilterWeight = 1.0f;
                    }
                });
                adb.show();
                break;
            case R.id.action_info:

                String convention;
                if (Constants.IS_AUTO_BUILDING_TYPE){
                    convention = myCountryOffset.getNumberingConvention();
                } else {
                    convention = "UK";
                }

                AlertDialog.Builder adb_info =new AlertDialog.Builder(UserActivity.this);
                adb_info.setTitle("Info");
                adb_info.setMessage(String.format("%s %.2f hPa\n" +
                                "%s %.2f hPa\n" +
                                "%s %.2f m\n" +
                                "%s %.2f m\n" +
                                "%s %.2f m\n" +
                                "%s %s\n" +
                                "\n\n" +
                                "%s\n" +
                                "%s\n" +
                                "%s",
                        "Current Pressure:", currPressure, "Sea Level Pressure:", referencePressure,
                        "Current Elevation:", userLevelAltitude, "Ground Elevation:", groundLevelAltitude,
                        "Offset to Ground:", Constants.OFFSET_TO_GOOGLE, "Floor Numbering Convention:", convention,
                        "Powered By Dark Sky", "Powered By Google", "Author: Montvydas Klumbys"));
                        adb_info.setPositiveButton("Ok", null);
                adb_info.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void setStatusBarTranslucent(boolean makeTranslucent) {
        if (makeTranslucent) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * Location provider - GoogleApiClient is created
     */
    private void initialiseLocationUpdater() {
        //Allows an app to access precise location, thus ask for permissions for that
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    Constants.MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }

        mLocationUpdater = new LocationUpdater(this, this);
        mLocationUpdater.createLocationProvider();
    }

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
    protected void onStart() {
        super.onStart();
        if (mLocationUpdater != null)
            mLocationUpdater.connectLocationProvider();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationUpdater.disconnectLocationProvider();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);

        if (!getOffsetCheck()){
            AlertDialog.Builder adb=new AlertDialog.Builder(UserActivity.this);
            adb.setTitle("Welcome!");
            adb.setMessage("As this is Your first time using the application, you need to calibrate it! Stay on the ground floor, keep the phone in a stable position and press 'Calibrate'! Don't worry, you can always re-calibrate it afterwards!");
            adb.setPositiveButton("Calibrate", new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //deleting everything means clearing all of the lists
                    isSavingGoogleOffset = true;
                    startScanForData();
                    offsetFilterWeight = 1.0f;
                    setOffsetCheck(true);
                }
            });
            adb.show();
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Constants.AUTO_UPDATE_INTERVAL = Integer.parseInt(prefs.getString(SettingsFragment.KEY_AUTO_UPDATE_INTERVAL, "60"));
        Constants.IS_AUTO_UPDATE_RUNNING = prefs.getBoolean(SettingsFragment.KEY_AUTO_UPDATE_CHECK, true);
        Constants.REFERENCE_TYPE = Integer.parseInt(prefs.getString(SettingsFragment.KEY_REF_TYPE, "0"));
        Constants.IS_AUTO_BUILDING_TYPE = prefs.getBoolean(SettingsFragment.KEY_BUILDING_TYPE, true);

        INTERVAL = 1000 * Constants.AUTO_UPDATE_INTERVAL;
        if (Constants.IS_AUTO_UPDATE_RUNNING) {
            startRepeatedUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        stopRepeatingUpdates();
    }

    static final String STATE_SCORE = "playerScore";

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putInt(STATE_SCORE, pStatus);

        super.onSaveInstanceState(savedInstanceState);
    }

    public void setNewGoogleOffset(float offset){
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(Constants.PREFS_GOOGLE_OFFSET, offset);
        // Commit the edits!
        editor.commit();
    }

    public float getGoogleOffset (){
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        return settings.getFloat(Constants.PREFS_GOOGLE_OFFSET, (float) Constants.OFFSET_TO_GOOGLE);
    }

    public boolean getOffsetCheck () {
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        return settings.getBoolean(Constants.PREFS_OFFSET_IS_SET, false);
    }

    public void setOffsetCheck (boolean check){
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.PREFS_OFFSET_IS_SET, check);
        // Commit the edits!
        editor.commit();
    }

    private static int INTERVAL = 1000 * Constants.AUTO_UPDATE_INTERVAL; //every minute
    Handler mHandler = new Handler();

    Runnable mHandlerTask = new Runnable()
    {
        @Override
        public void run() {
            startScanForData();
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
}
