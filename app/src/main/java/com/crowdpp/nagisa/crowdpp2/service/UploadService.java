package com.crowdpp.nagisa.crowdpp2.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.Log;
import android.widget.Toast;

import com.crowdpp.nagisa.crowdpp2.MainActivity;
import com.crowdpp.nagisa.crowdpp2.R;
import com.crowdpp.nagisa.crowdpp2.util.Constants;
import com.crowdpp.nagisa.crowdpp2.util.Now;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
    private String audioUrl = "http://54.196.39.156/uploadcrowdppnew.php";
    private String txtcallUrl = "http://54.196.39.156/uploadtxtcall.php";
    private final String TAG = "UploadService";
    private final int PERIODIC_EVENT_TIMEOUT = 60*60*1000;

    //private NetworkStateReceiver nr;
    private String uploadresult = null;
    private boolean canUpload = false;
    private boolean isActUpload = false;
    private boolean isAudioUpload = false;
    private boolean isTxtcallUpload = false;
    private Handler mPeriodicEventHandler;
    private SharedPreferences settings;
    private PowerManager.WakeLock wl;

    private String lineend = "\r\n";
    private String twoHyphens = "--";
    private String boundary = "******";
    private String selected, period, interval, duration, curr_hr;
    private boolean upload;
    private int start_hr, end_hr, interval_min;
    private int retry_timer_min = 5;
    NetworkStateReceiver nr;



    Queue<String> activityQueue = new LinkedList<String>();
    Queue<String> audioQueue = new LinkedList<String>();
    Queue<String> txtcallQueue = new LinkedList<String>();



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

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "No sleep");
        wl.acquire();

        // register preference change listener in service
        settings.registerOnSharedPreferenceChangeListener(listener);

        curr_hr = Now.getHour();

        mPeriodicEventHandler.postDelayed(doPeriodicTask, PERIODIC_EVENT_TIMEOUT);

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
        wl.release();
        super.onDestroy();
    }

    // upload text file
    public boolean uploadFile(){
        //Read all the Local un-uploaded Files into the list if any
        File note_dir = new File(Constants.activityPath);
        if (!note_dir.exists()){
            note_dir. mkdir();
        }

        String[] notes = note_dir.list();


        if (notes != null){
            // save all un-uploaded files to Queue
            Log.d(TAG, "Notes amount in directory: " + notes.length);
            for(int i = 0; i < notes.length; i++){
                activityQueue.offer(Constants.activityPath + "/" + notes[i]);
            }
        }

        Runnable act_run = new uploadActThread();
        Thread act_thread = new Thread(act_run);
        act_thread.start();
        try {
            // wait for thread
            act_thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        act_thread.interrupt();

        //read the dir for audio
        File audio_dir = new File(Constants.servicePath);
        if (!audio_dir.exists()){
            audio_dir. mkdir();
        }

        String[] audios = audio_dir.list();

        if (audios != null){
            // save all un-uploaded files to Queue
            Log.d(TAG, "Notes amount in directory: " + audios.length);
            for(int i = 0; i < audios.length; i++){
                audioQueue.offer(Constants.servicePath + "/" + audios[i]);
            }
        }

        Runnable audio_run = new uploadAudioThread();
        Thread audio_thread = new Thread(audio_run);
        audio_thread.start();
        try {
            // wait for thread
            audio_thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        audio_thread.interrupt();

        File txt_dir = new File(Constants.textPath);
        if (!txt_dir.exists()){
            txt_dir. mkdir();
        }

        String[] txts = txt_dir.list();

        if (txts != null){
            // save all un-uploaded files to Queue
            Log.d(TAG, "Notes amount in directory: " + txts.length);
            for(int i = 0; i < txts.length; i++){
                txtcallQueue.offer(Constants.textPath + "/" + txts[i]);
            }
        }

        File call_dir = new File(Constants.callPath);
        if (!call_dir.exists()){
            call_dir. mkdir();
        }

        String[] calls = call_dir.list();

        if (calls != null){
            // save all un-uploaded files to Queue
            Log.d(TAG, "Calls amount in directory: " + calls.length);
            for(int i = 0; i < calls.length; i++){
                txtcallQueue.offer(Constants.callPath + "/" + calls[i]);
            }
        }

        Runnable txtcall_run = new uploadTxtcallThread();
        Thread txtcall_thread = new Thread(txtcall_run);
        txtcall_thread.start();
        try {
            // wait for thread
            txtcall_thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        txtcall_thread.interrupt();

        //if upload the file and update the table successfully, return true.
        return isActUpload && isAudioUpload && isTxtcallUpload;
    }

    private class uploadActThread implements Runnable{
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
                        File archiveDir = new File(Constants.crowdppPath + "/archive/");
                        if(!archiveDir.exists()){
                            archiveDir.mkdirs();
                        }
                        if(uploadresult.contains("Success"))
                        {
                            activityQueue.poll();
                            isActUpload = true;
                            copyDirectory(srcfile, archiveDir);
                            RecursiveDeleteFile(srcfile);
                            Log.i("uploadThread","Delete Success");
                        }
                        else {
                            isActUpload = false;
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    private class uploadAudioThread implements Runnable{
        String filename;
        public void run(){
            try {
                while(!audioQueue.isEmpty()){
                    filename = audioQueue.peek();
                    Log.d("uploadAudioThread", "filename: " + filename);
                    File srcfile = new File(filename);
                    if(srcfile.exists()){
                        URL url = new URL(audioUrl);
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

                        Log.d("uploadAudioThread","uploadresult: "+uploadresult);
                        Log.d("uploadAudioThread", "uploaddone");
                        dos.close();
                        is.close();
                        File archiveDir = new File(Constants.crowdppPath + "/archive/");
                        if(!archiveDir.exists()){
                            archiveDir.mkdirs();
                        }
                        if(uploadresult.contains("Success"))
                        {
                            audioQueue.poll();
                            isAudioUpload = true;
                            copyDirectory(srcfile, archiveDir);
                            RecursiveDeleteFile(srcfile);
                            Log.i("uploadAudioThread","Delete Success");
                        }
                        else {
                            isAudioUpload = false;
                        }
                    }else{
                        break;
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    private class uploadTxtcallThread implements Runnable{
        String filename;
        public void run(){
            try {
                while(!txtcallQueue.isEmpty()){
                    filename = txtcallQueue.peek();
                    Log.d("uploadTxtcallThread", "filename: " + filename);
                    File srcfile = new File(filename);
                    if(srcfile.exists()){
                        URL url = new URL(txtcallUrl);
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

                        Log.d("uploadTxtcallThread","uploadresult: "+uploadresult);
                        Log.d("uploadTxtcallThread", "uploaddone");
                        dos.close();
                        is.close();
                        File archiveDir = new File(Constants.crowdppPath + "/archive/");
                        if(!archiveDir.exists()){
                            archiveDir.mkdirs();
                        }
                        if(uploadresult.contains("Success"))
                        {
                            txtcallQueue.poll();
                            isTxtcallUpload = true;
                            copyDirectory(srcfile, archiveDir);
                            RecursiveDeleteFile(srcfile);
                            Log.i("uploadTxtcallThread","Delete Success");
                        }
                        else {
                            isTxtcallUpload = false;
                        }
                    }else{
                        break;
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
                if(!uploadFile()) {// retry in retry_timer_min if it fails
                    mPeriodicEventHandler.postDelayed(doPeriodicTask, retry_timer_min * 60 * 1000);
                }else{
                    mPeriodicEventHandler.postDelayed(doPeriodicTask, PERIODIC_EVENT_TIMEOUT);
                }

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
    private void copyDirectory(File sourceLocation , File targetLocation)throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
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
