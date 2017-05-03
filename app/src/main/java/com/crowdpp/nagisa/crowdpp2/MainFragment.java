package com.crowdpp.nagisa.crowdpp2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.crowdpp.nagisa.crowdpp2.service.UploadService;

/**
 * Created by nagisa on 4/4/17.
 */

public class MainFragment extends Fragment implements View.OnClickListener {
    Button mActivityBtn, mSettingBtn, mHelpBtn, mLogBtn, maaa;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_fragment, container, false);

        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Activity Logger");

        mActivityBtn = (Button) rootView.findViewById(R.id.activity_btn);
        mActivityBtn.setOnClickListener(this);

        mSettingBtn = (Button) rootView.findViewById(R.id.setting_btn);
        mSettingBtn.setOnClickListener(this);

        mHelpBtn = (Button) rootView.findViewById(R.id.help_btn);
        mHelpBtn.setOnClickListener(this);

        maaa = (Button) rootView.findViewById(R.id.aaa);
        maaa.setOnClickListener(this);

        mLogBtn = (Button) rootView.findViewById(R.id.log_btn);
        mLogBtn.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_btn: {
                Intent countIntent = new Intent(getContext(), UploadService.class);
                getContext().startService(countIntent);
                Log.d("MainActivity", "Start service");
                Toast.makeText(getContext(), "Service is running!", Toast.LENGTH_LONG).show();
                break;
            }

            case R.id.setting_btn: {
                SettingFragment settingFragment = new SettingFragment();
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, settingFragment)
                        .addToBackStack(null)
                        .commit();

                break;
            }

            case R.id.help_btn: {
                HelpFragment helpFragment = new HelpFragment();
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, helpFragment)
                        .addToBackStack(null)
                        .commit();
                break;
            }

            case R.id.log_btn: {
                LogFragment logFragment = new LogFragment();
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, logFragment)
                        .addToBackStack(null)
                        .commit();
                break;
            }

            case R.id.aaa: {

                // 用来查看preference
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                // list
                String duration = sharedPref.getString("duration", "1");
                String selected = getResources().getStringArray(R.array.DurationArrays)[Integer.parseInt(duration)];

                // switch
                boolean b = sharedPref.getBoolean("upload", true);

                // dialog
                String period = sharedPref.getString("period", "9,21");
                Toast.makeText(getContext(), selected + (b ? " true" : " false") + period, Toast.LENGTH_LONG).show();
                break;
            }

            default:
                break;
        }

    }
}
