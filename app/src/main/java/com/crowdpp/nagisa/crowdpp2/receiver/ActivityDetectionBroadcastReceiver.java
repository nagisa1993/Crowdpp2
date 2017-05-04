package com.crowdpp.nagisa.crowdpp2.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

import com.crowdpp.nagisa.crowdpp2.MainActivity;
import com.crowdpp.nagisa.crowdpp2.MainFragment;
import com.google.android.gms.location.DetectedActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Activity Detection Broadcast Receiver
 * @author Haiyue Ma
 */

public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {

    //MainActivity mainContext;
    MainFragment mainFragmentContext;
    private static final String TAG = "BroadcastReceiver";
    private int confidence;
    String act, h, activityString, mostProbableActivity;
    ArrayList<DetectedActivity> detectedActivities;

    public ActivityDetectionBroadcastReceiver() {
        // make the class a static class so that it can be registered in Manifest
        super();
    }

    public ActivityDetectionBroadcastReceiver(MainFragment Context){
        mainFragmentContext = Context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        detectedActivities = intent.getParcelableArrayListExtra("com.crowdpp.nagisa.crowdpp2.ACTIVITY_RESULT");
        mostProbableActivity = intent.getExtras().getString("com.crowdpp.nagisa.crowdpp2.MOST_ACTIVITY_RESULT");
        Log.d(TAG, "Activities received by boardcastreceiver, sending back to MainActivity...");

        activityString  = "";
        for(DetectedActivity activity: detectedActivities){
            confidence = activity.getConfidence();
            act = mainFragmentContext.getDetectedActivity(activity.getType());
            activityString +=  "Activity: " + act + ", Confidence: " + confidence + "%\n";
        }
        activityString += "Most probable activity: " + mostProbableActivity;

        try {
            h = DateFormat.format("MM-dd-yyyyy-h-mmssaa", System.currentTimeMillis()).toString();
            File root = new File(Environment.getExternalStorageDirectory(), "Activity");
            if (!root.exists()) {
                root.mkdirs();
            }
            File filepath = new File(root, h + ".txt");  // file path to save
            FileWriter writer = new FileWriter(filepath);
            writer.append(activityString);
            writer.flush();
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
