package com.crowdpp.nagisa.crowdpp2.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sugan on 8/14/2017.
 */

public class CallJson extends JSONObject {
    public JSONObject makeJSONObject(String address, String date, String duration, String callerType) {

        JSONObject obj = new JSONObject();

        try {
            obj.put("address", address);
            obj.put("date", date);
            obj.put("duration", duration);
            obj.put("callerType", callerType);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }
}
