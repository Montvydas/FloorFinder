package com.monte.tangoapp;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by monte on 05/01/2017.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_AUTO_UPDATE_INTERVAL = "pref_key_update_interval";
    public static final String KEY_AUTO_UPDATE_CHECK = "pref_key_auto_update";
    public static final String KEY_REF_TYPE = "pref_key_ref_type";
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.preferences);
        this.initSummaries(this.getPreferenceScreen());

//        Preference connectionPref = findPreference(KEY_AUTO_UPDATE_INTERVAL);
//        SharedPreferences prefs = this.getPreferenceScreen().getSharedPreferences();
//        connectionPref.setSummary(prefs.getString(KEY_AUTO_UPDATE_INTERVAL, ""));

//        Log.e("interval?", prefs.getString(KEY_AUTO_UPDATE_INTERVAL, ""));

        this.getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Set the summaries of all preferences
     */
    private void initSummaries(PreferenceGroup pg) {
        for (int i = 0; i < pg.getPreferenceCount(); ++i) {
            Preference p = pg.getPreference(i);
            if (p instanceof PreferenceGroup)
                this.initSummaries((PreferenceGroup) p); // recursion
            else
                this.setSummary(p);
        }
    }

    /**
     * Set the summaries of the given preference
     */
    private void setSummary(Preference pref) {
        // react on type or key
        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
        }
    }

    /**
     * used to change the summary of a preference
     */
//    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
//        Preference pref = findPreference(key);

//    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference connectionPref = findPreference(key);
        switch (key){
            case KEY_AUTO_UPDATE_INTERVAL:
                connectionPref.setSummary(sharedPreferences.getString(key, ""));
                break;
            case KEY_REF_TYPE:
                this.setSummary(connectionPref);
                break;
        }
    }
//
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
