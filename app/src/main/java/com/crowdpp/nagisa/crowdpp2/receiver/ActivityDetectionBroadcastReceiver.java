package com.crowdpp.nagisa.crowdpp2.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;

import com.crowdpp.nagisa.crowdpp2.MainActivity;
import com.crowdpp.nagisa.crowdpp2.MainFragment;
import com.crowdpp.nagisa.crowdpp2.util.ActivityJson;
import com.google.android.gms.location.DetectedActivity;

import org.json.JSONObject;

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
    String act, h, date, time, mostProbableActivity, phoneType, confidence = "";
    ArrayList<DetectedActivity> detectedActivities;
    ArrayList<String> activities;


    public ActivityDetectionBroadcastReceiver() {
        // make the class a static class so that it can be registered in Manifest
        super();
    }

    public ActivityDetectionBroadcastReceiver(MainFragment Context, String pt){
        mainFragmentContext = Context;
        phoneType = pt;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        detectedActivities = intent.getParcelableArrayListExtra("com.crowdpp.nagisa.crowdpp2.ACTIVITY_RESULT");
        mostProbableActivity = intent.getExtras().getString("com.crowdpp.nagisa.crowdpp2.MOST_ACTIVITY_RESULT");
        Log.d(TAG, "Activities received by boardcastreceiver, sending back to MainActivity...");

        ActivityJson obj = new ActivityJson();
        confidence = "";
        activities = new ArrayList<>();

        date = DateFormat.format("MM-dd-yyyy", System.currentTimeMillis()).toString();
        time = DateFormat.format("h-mm-ssaa", System.currentTimeMillis()).toString();
        //activityString  = "";


        for(DetectedActivity activity: detectedActivities){
            confidence += activity.getConfidence() + ",";
            act = mainFragmentContext.getDetectedActivity(activity.getType());
            activities.add(act);
            //activityString +=  "Activity: " + act + ", Confidence: " + confidence + "%\n";
        }
        //activityString += "Most probable activity: " + mostProbableActivity;
        JSONObject jsonObject = obj.makeJSONObject(date, time, activities, confidence, mostProbableActivity);

        try {
            h = DateFormat.format("MM-dd-yyyyy-h-mmssaa", System.currentTimeMillis()).toString();
            File root = new File(Environment.getExternalStorageDirectory(), "Activity");
            if (!root.exists()) {
                root.mkdirs();
            }
            File filepath = new File(root, h + ".json");  // file path to save
            FileWriter writer = new FileWriter(filepath);
            writer.append(jsonObject.toString());
            writer.flush();
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
