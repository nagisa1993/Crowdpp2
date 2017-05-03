package com.crowdpp.nagisa.crowdpp2.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crowdpp.nagisa.crowdpp2.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nagisa on 5/1/17.
 */

public class LogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static int TYPE_SMS = 1, TYPE_CALL = 2;
    private Context mContext;
    List<Object> smsCallList = new ArrayList<>();

    public LogAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setCallSMSFeed(List<Object> smsCallList){
        this.smsCallList = smsCallList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        int layout = 0;
        RecyclerView.ViewHolder viewHolder;
        switch (viewType) {
            case TYPE_SMS: {
                layout = R.layout.log_sms;
                View smsView = LayoutInflater
                        .from(mContext)
                        .inflate(layout, null);
                viewHolder = new smsViewHolder(smsView);
                break;
            }
            case TYPE_CALL: {
                layout = R.layout.log_call;
                View callView = LayoutInflater
                        .from(mContext)
                        .inflate(layout, null);
                viewHolder = new callViewHolder(callView);
                break;
            }
            default:
                viewHolder = null;
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_SMS:
                Sms sms = (Sms) smsCallList.get(position);
                ((smsViewHolder) holder).showSmsDetail(sms);
                break;

            case TYPE_CALL:
                Call call = (Call) smsCallList.get(position);
                ((callViewHolder) holder).showCallDetail(call);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (smsCallList.get(position) instanceof Call) {
            return TYPE_CALL;
        } else if (smsCallList.get(position) instanceof Sms) {
            return TYPE_SMS;
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return smsCallList.size();
    }

    public class smsViewHolder extends RecyclerView.ViewHolder {
        private TextView smsName, smsBody, smsDate;

        public smsViewHolder(View itemView) {
            super(itemView);
            smsName = (TextView) itemView.findViewById(R.id.smsName);
            smsBody = (TextView) itemView.findViewById(R.id.smsBody);
            smsDate = (TextView) itemView.findViewById(R.id.smsDate);
        }

        public void showSmsDetail(Sms sms) {
            smsName.setText(sms.getName());
            smsBody.setText(sms.getBody());
            smsDate.setText(sms.getDate());
        }
    }

    public class callViewHolder extends RecyclerView.ViewHolder {
        private TextView callName, callDuration, callDate;

        public callViewHolder(View itemView) {
            super(itemView);
            callName = (TextView) itemView.findViewById(R.id.callName);
            callDuration = (TextView) itemView.findViewById(R.id.callDuration);
            callDate = (TextView) itemView.findViewById(R.id.callDate);
        }

        public void showCallDetail(Call call) {
            callName.setText(call.getName());
            callDate.setText(call.getDate());
            callDuration.setText(call.getDuration());
        }
    }
}
