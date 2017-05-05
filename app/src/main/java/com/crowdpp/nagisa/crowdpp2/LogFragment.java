package com.crowdpp.nagisa.crowdpp2;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.crowdpp.nagisa.crowdpp2.util.Call;
import com.crowdpp.nagisa.crowdpp2.util.LogAdapter;
import com.crowdpp.nagisa.crowdpp2.util.Sms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by nagisa on 4/30/17.
 */

public class LogFragment extends Fragment{
    private final String TAG = "LogFragment";
    private RecyclerView mRecyclerView;
    private List<Object> mCallSMSList;
    private LogAdapter mCallSMSAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View logView = inflater.inflate(R.layout.log_fragment, container, false);
        mRecyclerView = (RecyclerView) logView.findViewById(R.id.smsCallLog);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        feed();
        return logView;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Log");

        Toolbar toolbar = ((MainActivity) getActivity()).toolbar;
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
            }
        });
    }

    public void feed() {
        mCallSMSAdapter = new LogAdapter(getContext());
        mRecyclerView.setAdapter(mCallSMSAdapter);
        mCallSMSList = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm");

        // quiry sms
        Uri uriSms = Uri.parse("content://sms/inbox");
        Cursor cursor_sms = getActivity().getContentResolver().query(uriSms, new String[]{"_id", "address", "date", "body", "type"},null,null,null);

        cursor_sms.moveToFirst();
        while  (cursor_sms.moveToNext())
        {
            String address = cursor_sms.getString(1);
            String date = cursor_sms.getString(2);
            date = df.format(new Date(Long.parseLong(date)));
            String body = cursor_sms.getString(3);

            mCallSMSList.add(new Sms(address, body, date));
        }

        // quiry call log
        Uri allCalls = Uri.parse("content://call_log/calls");
        Cursor cursor_call = getActivity().getContentResolver().query(allCalls, null, null, null, android.provider.CallLog.Calls.DATE + " DESC");

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
            mCallSMSList.add(new Call(address, callerDate + ", " + callerType, duration + " sec"));
        }

        cursor_sms.close();
        cursor_call.close();

        // Set items to adapter
        mCallSMSAdapter.setCallSMSFeed(mCallSMSList);
        mCallSMSAdapter.notifyDataSetChanged();
    }
}
