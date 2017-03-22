package com.crowdpp.nagisa.crowdpp2;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

/**
 * Created by nagisa on 3/21/17.
 */

public class ActivityRecognizedService extends IntentService{
    private static final String TAG = "IntentService";

    public ActivityRecognizedService() {
        super("ActivityRecognizedService");
    }

    public ActivityRecognizedService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

            Log.d(TAG, "Activity_intent received, boardcasting activities...");
            Intent i = new Intent("com.crowdpp.nagisa.crowdpp2.ACTIVITY_ALL");
            i.putExtra("com.crowdpp.nagisa.crowdpp2.ACTIVITY_RESULT", detectedActivities);
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        }
    }
}
