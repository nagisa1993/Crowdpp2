package com.crowdpp.nagisa.crowdpp2.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import com.crowdpp.nagisa.crowdpp2.R;

/**
 * Defines the TimePreference method and constructor
 * Created by nagisa on 4/11/17.
 */

public class TimePreference extends DialogPreference {
    private String mTime = "9,21";
    private int mFromTime, mToTime;
    private int mDialogLayoutResId = R.layout.pref_dialog_time;

    public TimePreference(Context context) {
        this(context, null);
    }

    public TimePreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);

        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
    }

    public TimePreference(Context context, AttributeSet attrs,
                          int defStyleAttr) {
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public TimePreference(Context context, AttributeSet attrs,
                          int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        // Do custom stuff here
        // ...
        // read attributes etc.
    }

    public int getFromTime() { return mFromTime;}
    public int getToTime() {return mToTime;}

    public void setTime(String time) {
        mTime = time;
        mFromTime = Integer.parseInt(time.split(",")[0]);
        mToTime = Integer.parseInt(time.split(",")[1]);
        persistString(time);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        //return(a.getInt(index, 0));
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if(restoreValue) {
            setTime(getPersistedString(mTime));
        }
        else {
            setTime((String)defaultValue);
        }
    }

    @Override
    public int getDialogLayoutResource() {
        return mDialogLayoutResId;
    }

}
