package com.crowdpp.nagisa.crowdpp2.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by haiyue ma on 7/30/17.
 */

public class ActivityJson extends JSONObject {


    public JSONObject makeJSONObject(String location, String date, String time, ArrayList<String> Activity, String Confidence, String most) {

        JSONObject obj = new JSONObject();

        try {
            obj.put("location", location);
            obj.put("date", date);
            obj.put("time", time);
            obj.put("activity", Activity);
            obj.put("confidence", Confidence);
            obj.put("mostproac", most);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }
}
