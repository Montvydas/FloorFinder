package com.monte.tangoapp;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

/**
 * Created by monte on 05/01/2017.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_AUTO_UPDATE_INTERVAL = "pref_key_update_interval";
    public static final String KEY_AUTO_UPDATE_CHECK = "pref_key_auto_update";
    public static final String KEY_REF_TYPE = "pref_key_ref_type";
    public static final String KEY_REF_STATUS = "pref_key_ref_status";
    public static final String KEY_BUILDING_TYPE = "pref_key_building_type";

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.preferences);

        this.initSummaries(this.getPreferenceScreen());

        Preference connectionPref = findPreference(KEY_AUTO_UPDATE_INTERVAL);
        connectionPref.setSummary(getInterval() + " seconds");

//        SharedPreferences prefs = this.getPreferenceScreen().getSharedPreferences();
//        connectionPref.setSummary(prefs.getString(KEY_AUTO_UPDATE_INTERVAL, "") + " seconds");

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
        if (pref instanceof NumberPickerPreference){

            NumberPickerPreference picker = (NumberPickerPreference) pref;
            int index = NumberPickerPreference.getIndexOfInstance(getInterval());
            picker.setValue(index);
        }
        if (pref.getKey().equals(KEY_REF_STATUS)){

            pref.setSummary(String.format("%.2f m", Constants.OFFSET_TO_GOOGLE));
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference connectionPref = findPreference(key);
        switch (key){
            case KEY_AUTO_UPDATE_INTERVAL:
                setInterval(sharedPreferences.getString(key, ""));
                connectionPref.setSummary(sharedPreferences.getString(key, "") + " seconds");
                break;
            case KEY_REF_TYPE:
                this.setSummary(connectionPref);
                break;
            case KEY_BUILDING_TYPE:
                this.setSummary(connectionPref);
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

    public void setInterval(String intervalIndex){
        SharedPreferences settings = getActivity().getSharedPreferences(Constants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.PREFS_AUTO_UPDATE_INTERVAL, intervalIndex);
        // Commit the edits!
        editor.commit();
    }

    public String getInterval (){
        SharedPreferences settings = getActivity().getSharedPreferences(Constants.PREFS_NAME, 0);
        return settings.getString(Constants.PREFS_AUTO_UPDATE_INTERVAL, "60");
    }
}
