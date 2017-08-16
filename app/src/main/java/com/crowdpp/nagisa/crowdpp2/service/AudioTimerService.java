package com.crowdpp.nagisa.crowdpp2.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.crowdpp.nagisa.crowdpp2.MainActivity;
import com.crowdpp.nagisa.crowdpp2.R;
import com.crowdpp.nagisa.crowdpp2.audio.AudioRecorder;
import com.crowdpp.nagisa.crowdpp2.util.Constants;
import com.crowdpp.nagisa.crowdpp2.util.FileProcess;
import com.crowdpp.nagisa.crowdpp2.util.LocationTracker;
import com.crowdpp.nagisa.crowdpp2.util.NotificationDisplayer;
import com.crowdpp.nagisa.crowdpp2.util.Now;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;


public class AudioTimerService extends Service {
    AudioRecorder extAudioRecorder = null;

    private Handler mPeriodicEventHandler;
    private SharedPreferences settings;

    private String lineend = "\r\n";
    private String twoHyphens = "--";
    private String boundary = "******";
    private String selected, period, interval, duration, curr_hr, curr_min,curr_date;
    private  String audioDir;
    private boolean upload, location;
    private int start_hr, end_hr, interval_min, duration_min;
    static double latitude = -1;
    static  double longitude = -1;
    private String subjectNumber;
    private boolean debug = true;

    private Timer recordStartTimer, recordStopTimer;
    private  boolean recording;

    private LocationTracker loc;

    private PowerManager.WakeLock wl;

    NotificationManager mNotificationManager;

    public static final int NOTIFICATIN_ID = 101;
    NotificationDisplayer nd;

    private String wavFile;


    public AudioTimerService() {
    }

    @Override
    public void onCreate()	{

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        selected = settings.getString("interval", "0");
        interval = getResources().getStringArray(R.array.IntervalArrays)[Integer.parseInt(selected)];
        interval_min = Integer.parseInt(interval.split("\\s+")[0]);
        selected = settings.getString("duration", "0");
        duration = getResources().getStringArray(R.array.DurationArrays)[Integer.parseInt(selected)];
        duration_min = Integer.parseInt((interval.split("\\s+")[0]));
        period = settings.getString("period", "9,21");
        start_hr = Integer.parseInt(period.split(",")[0]);
        end_hr = Integer.parseInt(period.split(",")[1]);
        upload = settings.getBoolean("upload", true);
        Log.d("AudioTimerService", "setting: " + interval_min);
        curr_hr = Now.getHour();
        location = settings.getBoolean("location",true);
        subjectNumber = settings.getString("subjectNumber", "");
        File file = new File(Constants.servicePath);
        if(!file.exists()){
            file.mkdirs();
        }

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "No sleep");
        wl.acquire();

        java.util.Date dt = new java.util.Date();
        curr_hr = Now.getHour();
        curr_min = Now.getMinute();
        curr_date = Now.getDate();

        loc = new LocationTracker(getApplicationContext());

        // immediately start in debug mode
        if (debug) {
            interval_min = 3;
            duration_min = 1;
            int i = Integer.parseInt(curr_min);
            if (i < 59) {
                dt.setHours(Integer.parseInt(curr_hr));
                dt.setMinutes(i+1);
            }
            else if (i == 59) {
                dt.setHours(Integer.parseInt(curr_hr) + 1);
                dt.setMinutes(0);
            }
        }
        else {
            dt.setHours(Integer.parseInt(curr_hr) + 1);
            dt.setMinutes(0);
        }

        long interval_ms = (long) (interval_min * 60 * 1000);
        recordStartTimer = new Timer();
        recordStartTimer.schedule(new RecordingStartTask(), dt, interval_ms);
//        showAudioInfo();
        nd = new NotificationDisplayer(NOTIFICATIN_ID, this, "Audio", "Audio recording service is on.", R.drawable.ic_keyboard_voice);
        nd.showInfo();
        return START_STICKY;
    }


    /** This timer executes every interval_ms in a periodic manner to record audio and count speakers. */
    private class RecordingStartTask extends TimerTask {
        private Handler mHandler = new Handler(Looper.getMainLooper());

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    curr_hr = Now.getHour();
                    Log.i("RecordingStartTask", Integer.parseInt(curr_hr) + " is between " + start_hr + " and " + end_hr + "?");
                    if (Integer.parseInt(curr_hr) >= start_hr && Integer.parseInt(curr_hr) < end_hr) {
                        Log.i("RecordingStartTask", "In period.");
                        // get location information
                        if (location) {
                            loc.getLocation();
                            if (loc.canGetLocation()){
                                latitude = loc.getLatitude();
                                longitude = loc.getLongitude();
                            }
                            else {
                                latitude  = -1;
                                longitude = -1;
                            }
                        }
                        loc.stopUsingGPS();

                        wavFile = Constants.servicePath + "/Crowdpp"+"_"+subjectNumber+"_"+duration_min+"_"+latitude+"_"+longitude+"_"+FileProcess.newFileOnTime("wav");
                        Bundle mbundle = new Bundle();
                        mbundle.putString("audiopath", wavFile);
                        recording = true;
                        // start audio recording
                        Intent audioRecordIntent = new Intent(AudioTimerService.this, AudioRecordService.class);
                        audioRecordIntent.putExtras(mbundle);
                        Log.i("RecordingStartTask", "Recording");
                        startService(audioRecordIntent);
                        long duration_ms = (long) (duration_min * 60 * 1000);
                        recordStopTimer = new Timer();
                        recordStopTimer.schedule(new RecordStopTask(), duration_ms);
                    }
                    else {
                        Log.i("RecordingStartTask", "Out of time period.");
                    }
                }
            });

        }
    }

    /*
    Timer to stop recording after duration_min
     */
    private class RecordStopTask extends TimerTask {
        private Handler mHandler = new Handler(Looper.getMainLooper());
        @Override
        public void run() {
            mHandler.post(new Runnable(){

                @Override
                public void run() {
                    // stop audio recording
                    if(recording){
                        Log.i("AudioRecordService", "Stop audio recording");
                        Toast.makeText(AudioTimerService.this, "Stop audio recording...", Toast.LENGTH_SHORT).show();
                        // stop audio recording
                        Intent audioRecordIntent = new Intent(AudioTimerService.this, AudioRecordService.class);
                        stopService(audioRecordIntent);
                        recording = false;
                    }
                }

            });

        }
    }


    @Override
    public void onDestroy()	{
        Log.i("AudioTimerService", "Stop audio recording");
        Toast.makeText(this, "Stop audio recording...", Toast.LENGTH_SHORT).show();
        NotificationManager manager2 = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager2.cancelAll();
        if (recording) {
            Intent intent = new Intent(AudioTimerService.this, AudioRecordService.class);
            stopService(intent);
            recordStopTimer.cancel();

            //FileProcess.deleteFile(abs_wavFile);
            Log.i("AudioTimerServicek", "Cancel");
        }
        recordStartTimer.cancel();
        Log.i("AudioTimerService", "Cancel");
        wl.release();
        nd.stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }



}
