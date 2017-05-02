package com.crowdpp.nagisa.crowdpp2;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.crowdpp.nagisa.crowdpp2.receiver.ActivityDetectionBroadcastReceiver;
import com.crowdpp.nagisa.crowdpp2.service.ActivityRecognizedService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;

import com.crowdpp.nagisa.crowdpp2.service.UploadService;
/**
 * Main Activity
 * @author Haiyue Ma
 */

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    public TextView mActivityTextView;
    public Button mUploadBtn, mLogBtn;
    public Toolbar toolbar;
    private static final String TAG = "MyActivity";
    private ActivityDetectionBroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(savedInstanceState == null){
            MainFragment mainFragment = new MainFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, mainFragment)
                    .addToBackStack(null)
                    .commit();
        }

//        mActivityTextView = (TextView) findViewById(R.id.activities_textview);
//        mUploadBtn = (Button) findViewById(R.id.upload_btn);
//        mLogBtn = (Button) findViewById(R.id.log_btn);
//
//        // sync to the server
//        mUploadBtn.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View v) {
//                Intent countIntent = new Intent(MainActivity.this, UploadService.class);
//                startService(countIntent);
//                Log.d("MainActivity", "Start service");
//                Toast.makeText(MainActivity.this, "Service is running!", Toast.LENGTH_LONG).show();
//            }
//        });
//
//        mLogBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // quiry sms
//                Uri uriSms = Uri.parse("content://sms/inbox");
//                Cursor cursor_sms = getContentResolver().query(uriSms, new String[]{"_id", "address", "date", "body", "date", "type"},null,null,null);
//
//                cursor_sms.moveToFirst();
//                while  (cursor_sms.moveToNext())
//                {
//                    String address = cursor_sms.getString(1);
//                    String body = cursor_sms.getString(3);
//
//                    Log.d(TAG, "Mobile number: " + address);
//                    Log.d(TAG, "Text: " + body);
//                }
//
//                // quiry call log
//                Uri allCalls = Uri.parse("content://call_log/calls");
//                Cursor cursor_call = getContentResolver().query(allCalls, null, null, null, null);
//
//                cursor_call.moveToFirst();
//                while (cursor_call.moveToNext()) {
//                    String callerID = cursor_call.getString(cursor_call.getColumnIndex(CallLog.Calls._ID));
//                    String callerNumber = cursor_call.getString(cursor_call.getColumnIndex(CallLog.Calls.NUMBER));
//                    long callDateandTime = cursor_call.getLong(cursor_call.getColumnIndex(CallLog.Calls.DATE));
//                    long callDuration = cursor_call.getLong(cursor_call.getColumnIndex(CallLog.Calls.DURATION));
//                    int callType = cursor_call.getInt(cursor_call.getColumnIndex(CallLog.Calls.TYPE));
//                    if(callType == CallLog.Calls.INCOMING_TYPE)
//                    {
//                        //incoming call
//                    }
//                    else if(callType == CallLog.Calls.OUTGOING_TYPE)
//                    {
//                        //outgoing call
//                    }
//                    else if(callType == CallLog.Calls.MISSED_TYPE)
//                    {
//                        //missed call
//                    }
//                }
//            }
//        });

        // Create a GoogleApiClient instance
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // when connect() is called, need to create a PendingIntent that goes to the IntentService in onConnected()
        mGoogleApiClient.connect();

        // add broadcastreceiver
        mBroadcastReceiver = new ActivityDetectionBroadcastReceiver(this);
    }

    @Override
    protected void onResume() {
        // register boardcastreceiver
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter("com.crowdpp.nagisa.crowdpp2.ACTIVITY_ALL"));
    }

    @Override
    protected void onPause() {
        // unregister boardcastreceiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Closing...", Toast.LENGTH_SHORT).show();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_contact) {
            ContactFragment contactFragment = new ContactFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, contactFragment)
                    .addToBackStack(null)
                    .commit();
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            Toast.makeText(this, "Closing...", Toast.LENGTH_SHORT).show();
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // override for GoogleApiClient
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Connection Setup");
        Intent intent = new Intent(this, ActivityRecognizedService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
}
