package com.crowdpp.nagisa.crowdpp2.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.CallLog;
import android.support.v7.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.crowdpp.nagisa.crowdpp2.R;
import com.crowdpp.nagisa.crowdpp2.util.Call;
import com.crowdpp.nagisa.crowdpp2.util.CallJson;
import com.crowdpp.nagisa.crowdpp2.util.Constants;
import com.crowdpp.nagisa.crowdpp2.util.LogAdapter;
import com.crowdpp.nagisa.crowdpp2.util.NotificationDisplayer;
import com.crowdpp.nagisa.crowdpp2.util.Sms;
import com.crowdpp.nagisa.crowdpp2.util.TextJson;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TextCallLogService extends Service {
    private ArrayList<Sms> mSMSList;
    private ArrayList<Call> mCallList;
    private SharedPreferences settings;
    private String subjectNumber;
    private Timer fetchTimer;
    NotificationDisplayer nd;
    private int NOTIFICATION_ID = 102;
    public TextCallLogService() {
    }
    @Override
    public void onCreate(){

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        File txtpath  = new File(Constants.textPath);
        if(!txtpath.exists()){
            txtpath.mkdirs();
        }
        File callpath  = new File(Constants.callPath);
        if(!callpath.exists()){
            callpath.mkdirs();
        }
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        subjectNumber = settings.getString("subjectNumber", "");
        fetchTimer = new Timer();
        int interval_min = 120;
        int interval_ms = interval_min * 60 * 1000;
        fetchTimer.schedule(new FetchTimerTask(), interval_ms);
        nd = new NotificationDisplayer(NOTIFICATION_ID,this, "Text/Call", "Your text & call record is being logged", R.drawable.ic_log);
        nd.showInfo();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private class FetchTimerTask extends TimerTask{
        private Handler mHandler = new Handler(Looper.getMainLooper());
        @Override
        public void run(){
            ArrayList<Sms> smsList = new ArrayList<>();
            ArrayList<Call> callList = new ArrayList<>();
            feed(smsList, callList);
            File txtpath = new File(Constants.textPath);
            String[] txtfiles = txtpath.list();
            String phone_id = subjectNumber + "_" + Build.BRAND + Build.MODEL;
            TextJson tj  = new TextJson();
            try{
                if(txtfiles.length == 0){
                    String h = "txt" + phone_id + "_" + DateFormat.format("MM-dd-yyyy-h-mm-ssaa", System.currentTimeMillis()).toString();
                    File filepath = new File(txtpath, "/" + h + ".txt");  // file path to save
                    FileWriter writer = new FileWriter(filepath);

                    for(Sms msg: smsList){
                        JSONObject jsonObject = tj.makeJSONObject(msg.getName(), msg.getDate(), msg.getSmstype());
                        writer.append(jsonObject.toString());
                    }
                    writer.flush();
                    writer.close();

                }else {
                    String h = txtpath + "/" + txtfiles[txtfiles.length - 1];
                    FileWriter writer = new FileWriter(h, true);
                    for(Sms msg: smsList){
                        JSONObject jsonObject = tj.makeJSONObject(msg.getName(), msg.getDate(), msg.getSmstype());
                        writer.append(jsonObject.toString());
                    }
                    writer.flush();
                    writer.close();
                }

            }catch (IOException e){
                e.printStackTrace();
            }


            File callpath = new File(Constants.callPath);
            String[] callfiles = txtpath.list();
            CallJson cj  = new CallJson();
            try{
                if(callfiles.length == 0){
                    String h = "call" + phone_id + "_" + DateFormat.format("MM-dd-yyyy-h-mm-ssaa", System.currentTimeMillis()).toString();
                    File filepath = new File(txtpath, "/" + h + ".txt");  // file path to save
                    FileWriter writer = new FileWriter(filepath);

                    for(Call call: callList){
                        JSONObject jsonObject = cj.makeJSONObject(call.getName(), call.getDate(), call.getDuration(), call.getCallerType());
                        writer.append(jsonObject.toString());
                    }
                    writer.flush();
                    writer.close();

                }else {
                    String h = txtpath + "/" + txtfiles[txtfiles.length - 1];
                    FileWriter writer = new FileWriter(h, true);
                    for(Call call: callList){
                        JSONObject jsonObject = cj.makeJSONObject(call.getName(), call.getDate(), call.getDuration(), call.getCallerType());
                        writer.append(jsonObject.toString());
                    }
                    writer.flush();
                    writer.close();
                }

            }catch (IOException e){
                e.printStackTrace();
            }


            int interval_min = 120;
            int interval_ms = interval_min * 60 * 1000;
            fetchTimer.schedule(new FetchTimerTask(), interval_ms);

        }

    }


    public void feed(ArrayList<Sms> smsList, ArrayList<Call> callList) {
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm");

        // quiry sms in inbox
        Uri uriSmsinbox = Uri.parse("content://sms/inbox");
        Cursor cursor_smsinbox = getContentResolver().query(uriSmsinbox, new String[]{"_id", "address", "date", "body", "type"},null,null,null);

        cursor_smsinbox.moveToFirst();
        while  (cursor_smsinbox.moveToNext())
        {
            String address = cursor_smsinbox.getString(1);
            String date = cursor_smsinbox.getString(2);
            date = df.format(new Date(Long.parseLong(date)));
            String smstype = "inbox";

            smsList.add(new Sms(address, date, smstype));
        }


        //quiry sms in sent
        Uri uriSmssent = Uri.parse("content://sms/sent");
        Cursor cursor_smssent = getContentResolver().query(uriSmssent, new String[]{"_id", "address", "date", "body", "type"},null,null,null);

        cursor_smsinbox.moveToFirst();
        while  (cursor_smsinbox.moveToNext())
        {
            String address = cursor_smsinbox.getString(1);
            String date = cursor_smsinbox.getString(2);
            date = df.format(new Date(Long.parseLong(date)));
            String smstype = "sent";

            smsList.add(new Sms(address, date, smstype));
        }


        // quiry call log
        Uri allCalls = Uri.parse("content://call_log/calls");
        Cursor cursor_call = getContentResolver().query(allCalls, null, null, null, android.provider.CallLog.Calls.DATE + " DESC");

        cursor_call.moveToFirst();
        while (cursor_call.moveToNext()) {
            //String ID = cursor_call.getString(cursor_call.getColumnIndex(CallLog.Calls._ID));
            String address = cursor_call.getString(cursor_call.getColumnIndex(CallLog.Calls.NUMBER));
            String date = cursor_call.getString(cursor_call.getColumnIndex(android.provider.CallLog.Calls.DATE));
            String duration = cursor_call.getString(cursor_call.getColumnIndex(CallLog.Calls.DURATION));
            int type = cursor_call.getInt(cursor_call.getColumnIndex(CallLog.Calls.TYPE));
            String callerType = null, callerDate;
            callerDate = df.format(new Date(Long.parseLong(date)));
            if(type == CallLog.Calls.INCOMING_TYPE)
            {
                //incoming call
                callerType = "Incoming";
            }
            else if(type == CallLog.Calls.OUTGOING_TYPE)
            {
                //outgoing call
                callerType = "Outgoing";
            }
            else if(type == CallLog.Calls.MISSED_TYPE)
            {
                //missed call
                callerType = "Missed";
            }
            callList.add(new Call(address, callerDate, duration + " sec", callerType));
        }

        cursor_smsinbox.close();
        cursor_smssent.close();
        cursor_call.close();
    }

    @Override
    public void onDestroy(){
        Log.i("TextCallLogService", "Stop logging");
        Toast.makeText(this, "Stop txt & call logging...", Toast.LENGTH_SHORT).show();
        NotificationManager manager2 = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager2.cancelAll();
        fetchTimer.cancel();
        nd.stop();
    }
}
