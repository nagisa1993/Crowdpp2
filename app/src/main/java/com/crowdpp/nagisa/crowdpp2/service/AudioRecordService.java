package com.crowdpp.nagisa.crowdpp2.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.crowdpp.nagisa.crowdpp2.audio.AudioRecorder;

public class AudioRecordService extends Service {
    AudioRecorder extAudioRecorder = null;

    @Override
    public void onCreate()	{

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        String filename = bundle.getString("audiopath");
        // Uncompressed recording (WAV)
        extAudioRecorder = AudioRecorder.getInstanse(false);
        extAudioRecorder.setOutputFile(filename);
        extAudioRecorder.prepare();
        extAudioRecorder.start();
        Log.i("AudioRecordService", "Start audio recording");
        Toast.makeText(this, "Start audio recording...", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy()	{
        Log.i("AudioRecordService", "Stop audio recording");
        Toast.makeText(this, "Stop audio recording...", Toast.LENGTH_SHORT).show();
        extAudioRecorder.stop();
        extAudioRecorder.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}
