package com.crowdpp.nagisa.crowdpp2.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.Log;
import android.widget.Toast;

import com.crowdpp.nagisa.crowdpp2.R;
import com.crowdpp.nagisa.crowdpp2.util.Now;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Activity Upload Service
 * @author Haiyue Ma
 */

public class UploadService extends Service {
    private String actionUrl = "http://54.196.39.156/uploadActivity.php";
    private final String TAG = "UploadService";
    private final int PERIODIC_EVENT_TIMEOUT = 60*1000;

    //private NetworkStateReceiver nr;
    private String uploadresult = null;
    private boolean canUpload = false;
    private boolean isUpload = false;
    private Handler mPeriodicEventHandler;
    private SharedPreferences settings;

    private String lineend = "\r\n";
    private String twoHyphens = "--";
    private String boundary = "******";
    private String selected, period, interval, duration, curr_hr;
    private boolean upload;
    private int start_hr, end_hr, interval_min;
    NetworkStateReceiver nr;

    Queue<String> activityQueue = new LinkedList<String>();

    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            // listener implementation

            if (key.equals("interval")) {
                settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                selected = settings.getString("interval", "0");
                interval = getResources().getStringArray(R.array.IntervalArrays)[Integer.parseInt(selected)];
                interval_min = Integer.parseInt(interval.split("\\s+")[0]);
                Log.d("intervalchangedto", interval_min + "");
            }

        }
    };

    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Upload Service started!");

        // kill service by itself when uploading service finish
        //stopSelf();

        mPeriodicEventHandler = new Handler();

        // get settings
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        selected = settings.getString("interval", "0");
        interval = getResources().getStringArray(R.array.IntervalArrays)[Integer.parseInt(selected)];
        interval_min = Integer.parseInt(interval.split("\\s+")[0]);
//        selected = settings.getString("duration", "1");
//        duration = getResources().getStringArray(R.array.DurationArrays)[Integer.parseInt(selected)];
        period = settings.getString("period", "9,21");
        start_hr = Integer.parseInt(period.split(",")[0]);
        end_hr = Integer.parseInt(period.split(",")[1]);
        upload = settings.getBoolean("upload", true);
        Log.d("UploadService", "setting: " + interval_min);

        // register preference change listener in service
        settings.registerOnSharedPreferenceChangeListener(listener);

        curr_hr = Now.getHour();
        if(upload && Integer.parseInt(curr_hr) >= start_hr && Integer.parseInt(curr_hr) < end_hr) {
            // upload file every 1 minute
            mPeriodicEventHandler.postDelayed(doPeriodicTask, interval_min*60*1000);
        }
        else if(!upload) {
            stopSelf();
            Log.d("TAG", "Upload closed.");
        }
        else {
            Log.d("TAG", "Out of time period.");
        }

        //uploadFile();

        IntentFilter it = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        nr = new NetworkStateReceiver();
        registerReceiver(nr, it);
        return START_STICKY;
    }

    @Override
    public void onDestroy()	{
        mPeriodicEventHandler.removeCallbacks(doPeriodicTask);
        Log.i("Crowd++", "Service stop...");
        Toast.makeText(this, "Upload service stopping...", Toast.LENGTH_SHORT).show();
        unregisterReceiver(nr);
        super.onDestroy();
    }

    // upload text file
    public boolean uploadFile(){
        //Read all the Local un-uploaded Files into the list if any
        File note_dir = new File(Environment.getExternalStorageDirectory(), "Activity");
        if (!note_dir.exists()){
            note_dir.mkdir();
        }

        String[] notes = note_dir.list();
        Log.d(TAG, "Notes amount in directory: " + notes.length);

        if (notes != null){
            // save all un-uploaded files to Queue
            for(int i = 0; i < notes.length; i++){
                activityQueue.offer(Environment.getExternalStorageDirectory() + "/Activity/" + notes[i]);
            }
        }

        Runnable r = new uploadThread();
        Thread thread = new Thread(r);
        thread.start();
        try {
            // wait for thread
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thread.interrupt();

        //if upload the file and update the table successfully, return true.
        return isUpload;
    }

    private class uploadThread implements Runnable{
        String filename;
        public void run(){
            try {
                while(!activityQueue.isEmpty()){
                    filename = activityQueue.peek();
                    Log.d("uploadThread", "filename: " + filename);
                    File srcfile = new File(filename);
                    if(srcfile.exists()){
                        URL url = new URL(actionUrl);
                        HttpURLConnection httpURLConnection = (HttpURLConnection) url
                                .openConnection();
                        httpURLConnection.setDoInput(true);
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.setUseCaches(false);
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                        httpURLConnection.setRequestProperty("Charset", "UTF-8");
                        httpURLConnection.setRequestProperty("Content-Type",
                                "multipart/form-data;boundary=" + boundary);
                        DataOutputStream dos = new DataOutputStream(
                                httpURLConnection.getOutputStream());

                        dos.writeBytes(twoHyphens + boundary + lineend);
                        dos.writeBytes("Content-Disposition: form-data; name=\"fileToUpload\"; filename=\""
                                + filename.substring(filename.lastIndexOf("/") + 1)
                                + "\""
                                + lineend);
                        dos.writeBytes(lineend);
                        FileInputStream fis = new FileInputStream(filename);
                        byte[] buffer = new byte[8192];
                        int count = 0;
                        while ((count = fis.read(buffer)) != -1)
                        {
                            dos.write(buffer, 0, count);
                        }
                        fis.close();
                        dos.writeBytes(lineend);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineend);
                        dos.flush();
                        InputStream is = httpURLConnection.getInputStream();
                        InputStreamReader isr = new InputStreamReader(is, "utf-8");
                        BufferedReader br = new BufferedReader(isr);
                        uploadresult = br.readLine();

                        Log.d("uploadresult","uploadresult: "+uploadresult);
                        Log.d("uploadafter", "uploaddone");
                        dos.close();
                        is.close();

                        if(uploadresult.contains("Success"))
                        {
                            activityQueue.poll();
                            isUpload = true;
                            RecursiveDeleteFile(srcfile);
                            Log.i("uploadThread","Delete Success");
                        }
                        else {
                            isUpload = false;
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    // periodically upload files (if any files are uploaded unsuccessfully due to no-network or other error)
    private Runnable doPeriodicTask = new Runnable(){
        public void run(){
            //your action here
            try{
                Log.d("DoPeriodicTask", "ready to upload");
                if(uploadFile()){
                    canUpload = true;
                }
                else
                    canUpload = false;
                mPeriodicEventHandler.postDelayed(doPeriodicTask, interval_min*60*1000);
            }  catch (Exception ex) {
                Log.e("PeriodicTask", ex.toString());
            }
        }
    };

    // network state boardcast receiver
    public class NetworkStateReceiver extends BroadcastReceiver {
        public NetworkStateReceiver() {
            super();
        }

        public void onReceive(Context context, Intent intent) {
            Log.d("app","Network connectivity change");
            if(intent.getExtras()!=null) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
                if(ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
                    uploadFile();
                    Log.d("NetworkStateReceiver", "Network "+ ni.getTypeName() + " connected");
                }
            }
            if(intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY,Boolean.FALSE)) {
                Log.d("NetworkStateReceiver: ", "There's no network connectivity");
            }
        }
    }

    private void RecursiveDeleteFile(File file){
        if(file.isFile()){
            file.delete();
            return;
        }
        if(file.isDirectory()){
            File[] childFile = file.listFiles();
            if(childFile == null || childFile.length == 0){
                file.delete();
                return;
            }
            for(File f : childFile){
                RecursiveDeleteFile(f);
            }
            file.delete();
        }
    }
}
