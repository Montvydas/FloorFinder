package com.monte.tangoapp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClientQuery {
    public String getQueryResult(String UrlAddress) {
        HttpURLConnection httpConnection = null ;
        InputStream inputStream = null;

        try {
            httpConnection = (HttpURLConnection) ( new URL(UrlAddress)).openConnection();
//            httpConnection.setRequestMethod("GET");
//            httpConnection.setDoInput(true);
//            httpConnection.setDoOutput(true);
//            httpConnection.connect();

            StringBuffer buffer = new StringBuffer();
            inputStream = httpConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while (  (line = br.readLine()) != null )
                buffer.append(line + "\r\n");

            inputStream.close();
            httpConnection.disconnect();
            return buffer.toString();
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
        finally {
            try { inputStream.close(); } catch(Throwable t) {}
            try { httpConnection.disconnect(); } catch(Throwable t) {}
        }
        return null;
    }
}