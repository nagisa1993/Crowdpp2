package com.crowdpp.nagisa.crowdpp2;


import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;

import com.crowdpp.nagisa.crowdpp2.service.UploadService;
import com.crowdpp.nagisa.crowdpp2.util.TimePreference;

/**
 * Created by nagisa on 4/4/17.
 */

public class SettingFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingFragment()).commit();
//    }

    private final String TAG = "SettingFragment";
    TimePreference timePreference;
    SharedPreferences sharedPreferences;
    private String period, start_hr, end_hr;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.preferences);
        Log.d("Setting Fragment", "oncreatepreference");
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Setting");

//            FragmentManager fm = getFragmentManager();
//            Log.d("click", Integer.toString(fm.getBackStackEntryCount()));

        Toolbar toolbar = ((MainActivity) getActivity()).toolbar;
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
            }
        });

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Setting Fragment", "oncreate");
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
        timePreference = (TimePreference) findPreference("period");
        period = sharedPreferences.getString("period", "9,21");
        start_hr = period.split(",")[0];
        end_hr = period.split(",")[1];
        timePreference.setSummary(start_hr + ":00 - " + end_hr + ":00");
    }

    @Override
    public void onResume() {
        super.onResume();
        Toolbar toolbar = ((MainActivity) getActivity()).toolbar;
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
            }
        });
        //unregister the preferenceChange listener
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregister the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("interval")) {
            ListPreference pref = (ListPreference) findPreference(key);
            int prefIndex = pref.findIndexOfValue(sharedPreferences.getString(key, ""));
            String str = pref.getEntries()[prefIndex].toString();
            Log.d("preferencechangedto", str);
        }

        if(key.equals("period")) {
            period = sharedPreferences.getString("period", "9,21");
            start_hr = period.split(",")[0];
            end_hr = period.split(",")[1];
            timePreference.setSummary(start_hr + ":00 - " + end_hr + ":00");
        }

        if (key.equals("upload")) {
            SwitchPreferenceCompat pref = (SwitchPreferenceCompat) findPreference(key);
            boolean checked = pref.isChecked();
            Log.d(TAG, "upload switch: " + (checked ? "true" : "false"));
            if(checked) {
                if(!isServiceRunning(UploadService.class)) {
                    Intent countIntent = new Intent(getContext(), UploadService.class);
                    getContext().startService(countIntent);
                    Log.d(TAG, "Start upload service in background");
                }
            }
            else {
                if(isServiceRunning(UploadService.class)) {
                    getActivity().stopService(new Intent(getActivity(), UploadService.class));
                }
                Log.d(TAG, "Upload service unenabled");
            }
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {

        // Try if the preference is one of our custom Preferences
        DialogFragment dialogFragment = null;
        if (preference instanceof TimePreference) {
            // Create a new instance of TimePreferenceDialogFragment with the key of the related
            // Preference
            dialogFragment = TimePreferenceDialogFragmentCompat.newInstance(preference.getKey());
        }


        if (dialogFragment != null) {
            // The dialog was created (it was one of our custom Preferences), show the dialog for it
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(), "android.support.v7.preference" +
                    ".PreferenceFragment.DIALOG");
        } else {
            // Dialog creation could not be handled here. Try with the super method.
            super.onDisplayPreferenceDialog(preference);
        }

    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
