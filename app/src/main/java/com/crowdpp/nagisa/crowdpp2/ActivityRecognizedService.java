package com.crowdpp.nagisa.crowdpp2;

import android.app.IntentService;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.crowdpp.nagisa.crowdpp2.db.DataBaseHelper;
import com.crowdpp.nagisa.crowdpp2.util.Constants;
import com.crowdpp.nagisa.crowdpp2.util.Now;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Activity Recognized Service
 * @author Haiyue Ma
 */

public class ActivityRecognizedService extends IntentService{

    private static final String TAG = "IntentService";

    static long sys_time;
    static String date, start, end, filename;

    public Queue<String> activityFilename = new LinkedList<String>();

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
            String mostProbableActivity = getDetectedActivity(result.getMostProbableActivity().getType());
//            Log.d(TAG, "get activity");
//
//            date = Now.getDate();
//            start = Now.getTimeOfDay();
//            Log.d(TAG, "start time" + start);
//            end = Now.getTimeOfDay();

            Log.d(TAG, "Activity_intent received, boardcasting activities...");
            Intent i = new Intent("com.crowdpp.nagisa.crowdpp2.ACTIVITY_ALL");
            i.putExtra("com.crowdpp.nagisa.crowdpp2.ACTIVITY_RESULT", detectedActivities);
            i.putExtra("com.crowdpp.nagisa.crowdpp2.MOST_ACTIVITY_RESULT", mostProbableActivity);
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        }
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
}
