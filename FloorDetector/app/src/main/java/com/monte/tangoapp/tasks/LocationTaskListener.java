package com.monte.tangoapp.tasks;

import android.location.Location;

/**
 * Created by monte on 05/01/2017.
 */
public interface LocationTaskListener {
    void onLocationUpdated(Location location);
    void onConnected();
}
