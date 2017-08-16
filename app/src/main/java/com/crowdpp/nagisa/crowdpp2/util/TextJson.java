package com.crowdpp.nagisa.crowdpp2.util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sugan on 8/14/2017.
 */

public class TextJson extends JSONObject {
    public JSONObject makeJSONObject(String address, String date, String smstype) {

        JSONObject obj = new JSONObject();

        try {
            obj.put("address", address);
            obj.put("date", date);
            obj.put("smstype", smstype);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }
}
