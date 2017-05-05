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
//        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{

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
    }

    @Override
    protected void onResume() {
        // register boardcastreceiver
        super.onResume();
        //LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter("com.crowdpp.nagisa.crowdpp2.ACTIVITY_ALL"));
    }

    @Override
    protected void onPause() {
        // unregister boardcastreceiver
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
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
}
