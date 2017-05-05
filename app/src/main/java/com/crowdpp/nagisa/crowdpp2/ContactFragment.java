package com.crowdpp.nagisa.crowdpp2;



import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static android.app.Activity.RESULT_OK;

/**
 * Created by nagisa on 4/4/17.
 */

public class ContactFragment extends Fragment implements View.OnClickListener{
    FloatingActionButton mMailBtn;
    private static final int MY_REQUEST_CODE = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contactView = inflater.inflate(R.layout.contact_fragment, container, false);
        mMailBtn = (FloatingActionButton) contactView.findViewById(R.id.fab);
        mMailBtn.setOnClickListener(this);
        return contactView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Contact");
        FragmentManager fm = getFragmentManager();
        Log.d("clickincontact", Integer.toString(fm.getBackStackEntryCount()));

        Toolbar toolbar = ((MainActivity) getActivity()).toolbar;
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
//        FragmentManager fm = getFragmentManager();
//        Log.d("clickinlog", Integer.toString(fm.getBackStackEntryCount()));
        Toolbar toolbar = ((MainActivity) getActivity()).toolbar;
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab: {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "sugangli@winlab.rutgers.edu" });
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Crowd++2 bug report");
                sendIntent.setType("message/rfc822");
                startActivity(Intent.createChooser(sendIntent, "Send via"));
                break;
            }
        }
    }
}
