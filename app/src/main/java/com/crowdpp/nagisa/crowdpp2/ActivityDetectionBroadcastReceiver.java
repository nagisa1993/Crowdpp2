package com.crowdpp.nagisa.crowdpp2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

/**
 * Created by nagisa on 3/22/17.
 */

public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {
    MainActivity mainContext;
    private static final String TAG = "BroadcastReceiver";

    public ActivityDetectionBroadcastReceiver() {
        // make the class a static class so that it can be registered in Manifest
        super();
    }

    public ActivityDetectionBroadcastReceiver(MainActivity Context){
        mainContext = Context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ArrayList<DetectedActivity> detectedActivities = intent.getParcelableArrayListExtra("com.crowdpp.nagisa.crowdpp2.ACTIVITY_RESULT");
        String activityString = "";
        Log.i(TAG, "Activities received by boardcastreceiver, sending back to MainActivity...");
        for(DetectedActivity activity: detectedActivities){
            activityString +=  "Activity: " + mainContext.getDetectedActivity(activity.getType()) + ", Confidence: " + activity.getConfidence() + "%\n";
        }
        mainContext.mActivityTextView.setText(activityString);
    }
}
