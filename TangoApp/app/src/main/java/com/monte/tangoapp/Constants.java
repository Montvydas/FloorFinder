package com.monte.tangoapp;

/**
 * Created by monte on 05/01/2017.
 */
public class Constants {
//    private static Constants ourInstance = new Constants();
//
//    public static Constants getInstance() {
//        return ourInstance;
//    }
//
//    private Constants() {
//    }

    public static boolean IS_AUTO_UPDATE_RUNNING = true;
    public static int AUTO_UPDATE_INTERVAL = 60;
    public static final String PREFS_AUTO_UPDATE_INTERVAL = "update_interval";
    public static int REFERENCE_TYPE = 0;
    public static boolean IS_AUTO_BUILDING_TYPE = true;

    public static final double EDINBURGH_STATION_OFFSET = 11000;
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String PREFS_GOOGLE_OFFSET = "google_offset";
    public static final String PREFS_OFFSET_IS_SET = "offset_set";
    public static final String PREFS_BUILDING_TYPE = "building_type";


    public static float OFFSET_TO_GOOGLE = 16.5f;

    //constants used when requesting permissions
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_INTERNET = 2;
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3;
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 4;

    //Different API base addresses and keys, which will be used to built the full request address
    //Open weather API info; no longer used, but given for informative purposes
    public static final String BASE_URL_OPEN_WEATHER_MAP = "http://api.openweathermap.org/data/2.5/weather?";
    public static final String API_KEY_OPEN_WEATHER_MAP = "***REMOVED***";

    //Google elevation API info;
    public static final String BASE_URL_GOOGLE_ELEVATION = "https://maps.googleapis.com/maps/api/elevation/json?";
    public static final String API_KEY_GOOGLE_ELEVATION = "***REMOVED***";

    //Forecast.io API info;
    public static final String BASE_URL_FORECAST = "https://api.forecast.io/forecast/";
    public static final String API_KEY_FORECAST = "***REMOVED***";


    public static final String BASE_URL_SPARK_= "http://data.sparkfun.com/";
    public static final String API_KEY_PUBLIC_SPARK = "***REMOVED***";
    public static final String API_KEY_PRIVATE_SPARK = "***REMOVED***";


    public static String[] getSparkFunPullUrl (String baseAddress, String apiKey){
        long time = 1482074058;
        long unixTime = System.currentTimeMillis() / 1000L - 600;   //10min before data
        return new String[] {baseAddress + "output/" + apiKey + ".json?gt[time]=" + String.valueOf(unixTime)};
    }

    public static final String BASE_URL_THINGSPEAK= "https://api.thingspeak.com/";
    public static final String CHANNEL_THINGSPEAK= "116015";
    public static final String API_KEY_PUBLIC_THINGSPEAK = "***REMOVED***";

    public static String[] getThingSpeakPullUrl (String baseAddress, String apiKey, String channelID){
        return new String[] {baseAddress + "channels/" + channelID + "/feeds/last.json?api_key=" + apiKey};
    }

//    https://api.thingspeak.com/channels/116015/feeds/last.json?api_key=SN8VGR8P61B3OJUT

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
    public static String [] getOpenWeatherMapUrl (String baseAddress, String apiKey, double lat, double lon){
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
    public static String[] getGoogleElevationUrl (String baseAddress, String apiKey, double lat, double lon){
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
    public static String [] getForecastUrl (String baseAddress, String apiKey, double lat, double lon){
        //Forecast.io additionally can specify flags to request less information
        long unixTime = System.currentTimeMillis() / 1000L;
//        unixTime = 1483457110;
        return new String[] {baseAddress + apiKey + "/" + lat + "," + lon + "?units=si&exclude=minutely,hourly,daily,alerts,flags"};
    }
}
