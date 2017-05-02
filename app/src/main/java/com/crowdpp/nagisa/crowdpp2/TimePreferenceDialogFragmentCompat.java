package com.crowdpp.nagisa.crowdpp2;

import android.os.Bundle;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.appyvet.rangebar.RangeBar;
import com.crowdpp.nagisa.crowdpp2.util.TimePreference;

/** Bind NumberPicker with Dialog preference
 * Created by nagisa on 4/11/17.
 */

public class TimePreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    private RangeBar mRangeBar;
    TextView mFromText;
    TextView mToText;
    int mFromTime, mToTime;

    public static TimePreferenceDialogFragmentCompat newInstance(String key) {
        final TimePreferenceDialogFragmentCompat
                fragment = new TimePreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mRangeBar = (RangeBar) view.findViewById(R.id.rangebar);
        mFromText = (TextView) view.findViewById(R.id.from_text);
        mToText = (TextView) view.findViewById(R.id.to_text);

        // Exception: There is no Rangebar with the id 'edit' in the dialog.
        if (mRangeBar == null) {
            throw new IllegalStateException("Dialog view must contain a TimePicker with id 'rangebar'");
        }

        // Get the time from the related Preference
        Integer lastFromTime = null;
        Integer lastToTime = null;

        DialogPreference preference = getPreference();
        if (preference instanceof TimePreference) {
            lastFromTime = ((TimePreference) preference).getFromTime();
            lastToTime = ((TimePreference) preference).getToTime();
        }

        Log.d("COMPAT", Integer.toString(lastFromTime) + " " + Integer.toString(lastToTime));
        // Set the time to the rangebar
        if (lastFromTime != null && lastToTime != null) {
            mRangeBar.setRangePinsByValue((float)lastFromTime, (float)lastToTime);
            mFromText.setText(lastFromTime + ":00");
            mToText.setText(lastToTime + ":00");
        }

        mRangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,
                                              int rightPinIndex, String leftPinValue,
                                              String rightPinValue) {
                mFromTime = leftPinIndex + 1;
                mToTime = rightPinIndex + 1;
                Log.d("NEW TIME", Integer.toString(mFromTime) + " " + Integer.toString(mToTime));
                mFromText.setText(mFromTime + ":00");
                mToText.setText(mToTime + ":00");
            }

        });
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            DialogPreference preference = getPreference();
            if (preference instanceof TimePreference) {
                final TimePreference timePreference = ((TimePreference) preference);
                // This allows the client to ignore the user value.
                if (timePreference.callChangeListener(mFromTime) || timePreference.callChangeListener(mToTime)) {
                    // Save the value
                    String time = Integer.toString(mFromTime) + "," + Integer.toString(mToTime);
                    timePreference.setTime(time);
                }
            }
        }
    }
}
