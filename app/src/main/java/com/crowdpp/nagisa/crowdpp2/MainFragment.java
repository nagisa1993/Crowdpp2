package com.crowdpp.nagisa.crowdpp2;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.crowdpp.nagisa.crowdpp2.receiver.ActivityDetectionBroadcastReceiver;
import com.crowdpp.nagisa.crowdpp2.service.ActivityRecognizedService;
import com.crowdpp.nagisa.crowdpp2.service.AudioTimerService;
import com.crowdpp.nagisa.crowdpp2.service.TextCallLogService;
import com.crowdpp.nagisa.crowdpp2.service.UploadService;
import com.crowdpp.nagisa.crowdpp2.util.NotificationDisplayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;

/**
 * Created by nagisa on 4/4/17.
 */

public class MainFragment extends Fragment implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    Button mActivityBtn, mSettingBtn, mHelpBtn, mLogBtn, mAudioBtn;
    private final String TAG = "MainFragment";
    private SharedPreferences settings;
    private boolean upload;
    private GoogleApiClient mGoogleApiClient;
    private ActivityDetectionBroadcastReceiver mBroadcastReceiver;
    String phone_id;
    private View view;
    private final static int NOTIFICATIN_ID = 100;
    private NotificationManager mNotificationManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "oncreate in main fragment");
        // Create a GoogleApiClient instance
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_fragment, container, false);
        Log.d(TAG, "oncreateview in main fragment");

        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Activity Logger");

        mActivityBtn = (Button) rootView.findViewById(R.id.activity_btn);
        mActivityBtn.setOnClickListener(this);

        mSettingBtn = (Button) rootView.findViewById(R.id.setting_btn);
        mSettingBtn.setOnClickListener(this);

        mHelpBtn = (Button) rootView.findViewById(R.id.help_btn);
        mHelpBtn.setOnClickListener(this);

        mLogBtn = (Button) rootView.findViewById(R.id.log_btn);
        mLogBtn.setOnClickListener(this);

        mAudioBtn = (Button) rootView.findViewById(R.id.audio_btn);
        mAudioBtn.setOnClickListener(this);

        settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        upload = settings.getBoolean("upload", true);

        if(upload) {
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


        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_btn: {
                String subject_id = settings.getString("subjectNumber", "");
                phone_id = subject_id + "_" + Build.BRAND + Build.MODEL;
                if(!mGoogleApiClient.isConnected()) {
                    mBroadcastReceiver = new ActivityDetectionBroadcastReceiver(this, phone_id);
                    LocalBroadcastManager.getInstance(getContext()).registerReceiver(mBroadcastReceiver, new IntentFilter("com.crowdpp.nagisa.crowdpp2.ACTIVITY_ALL"));
                    mGoogleApiClient.connect();
//                    nd.showInfo();
                    Toast.makeText(getContext(), "Activity collection begins.", Toast.LENGTH_SHORT).show();
                }
                else {
                    mGoogleApiClient.disconnect();
                    Toast.makeText(getContext(), "Activity collection is closed.", Toast.LENGTH_SHORT).show();
                    LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBroadcastReceiver);
                }
                break;
            }

            case R.id.setting_btn: {
                SettingFragment settingFragment = new SettingFragment();
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, settingFragment)
                        .addToBackStack(null)
                        .commit();

                break;
            }

            case R.id.help_btn: {
                HelpFragment helpFragment = new HelpFragment();
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, helpFragment)
                        .addToBackStack(null)
                        .commit();
                break;
            }
            case R.id.audio_btn:{
                if(!isServiceRunning(AudioTimerService.class)){
                    Intent recordIntent = new Intent(getActivity(), AudioTimerService.class);
                    getActivity().startService(recordIntent);
                }else {
                    getActivity().stopService(new Intent(getActivity(), AudioTimerService.class));
                }

                break;
            }
            case R.id.log_btn: {
                if(!isServiceRunning(TextCallLogService.class)){
                    Intent logIntent = new Intent(getActivity(), TextCallLogService.class);
                    getActivity().startService(logIntent);
                }else{
                    getActivity().stopService(new Intent(getActivity(), TextCallLogService.class));
                }
                break;
            }

            default:
                break;
        }

    }

    @Override
    public void onResume() {
        // register boardcastreceiver
        super.onResume();

    }

    @Override
    public void onPause() {
        // unregister boardcastreceiver
        //LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "ondestroyview in main fragment");
    }

    // override for GoogleApiClient
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Connection Setup");
        Intent intent = new Intent(getContext(), ActivityRecognizedService.class);
        PendingIntent pendingIntent = PendingIntent.getService(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 10000, pendingIntent);
        Log.d(TAG, "Intent Setup");

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
        Log.d(TAG, "Connection Setup again");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection Failed. Error: " + connectionResult.getErrorMessage());
    }

    // return different kind of activity according to activityresult
    public String getDetectedActivity(int detectedActivityType) {
        Resources resources = this.getResources();
        switch(detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return resources.getString(R.string.in_vehicle);
            case DetectedActivity.ON_BICYCLE:
                return resources.getString(R.string.on_bicycle);
            case DetectedActivity.ON_FOOT:
                return resources.getString(R.string.on_foot);
            case DetectedActivity.RUNNING:
                return resources.getString(R.string.running);
            case DetectedActivity.WALKING:
                return resources.getString(R.string.walking);
            case DetectedActivity.STILL:
                return resources.getString(R.string.still);
            case DetectedActivity.TILTING:
                return resources.getString(R.string.tilting);
            case DetectedActivity.UNKNOWN:
                return resources.getString(R.string.unknown);
            default:
                return resources.getString(R.string.unidentifiable_activity, detectedActivityType);
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
