package com.monte.pressurestation;

import android.app.Activity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;


public class HttpClientQuery {

    static {
        HttpsURLConnection.setDefaultSSLSocketFactory(new NoSSLv3Factory());}
    //pass url and collect the received date in a form of string
    public String getQueryResult(String UrlAddress) {
        HttpURLConnection httpConnection = null ;
        InputStream inputStream = null;
        try {
//            SSLContext sslcontext = SSLContext.getInstance("TLSv1");
//
//            sslcontext.init(null,
//                    null,
//                    null);
//            SSLSocketFactory NoSSLv3Factory = new NoSSLv3SocketFactory(sslcontext.getSocketFactory());

//            HttpsURLConnection.setDefaultSSLSocketFactory(NoSSLv3Factory);
            //http connection is created from the url address
            httpConnection = (HttpURLConnection) ( new URL(UrlAddress)).openConnection();
            //set request to GET, which gets the queried information
            httpConnection.setRequestMethod("POST");
//            httpConnection.setRequestMethod("POST");
            //connect to the server
            httpConnection.connect();

            inputStream = httpConnection.getInputStream();
            //create a buffer to store the in-comming data
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            //finally translate the information line by line into a string buffer
            StringBuffer buffer = new StringBuffer();
            String line = null;
            while (  (line = br.readLine()) != null )
                buffer.append(line + "\r\n");

            inputStream.close();
            httpConnection.disconnect();
            Log.e("parsed", buffer.toString());

            //return buffer in a form of string
            return buffer.toString();
        }
        catch(Throwable t) {
            Log.e("error", "while trying");
            t.printStackTrace();
        }
        finally {
            //finally close the connection and input stream
            try { inputStream.close(); } catch(Throwable t) {}
            try { httpConnection.disconnect(); } catch(Throwable t) {}
        }
        return null;
    }
}