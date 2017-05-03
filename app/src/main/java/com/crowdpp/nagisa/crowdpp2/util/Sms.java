package com.crowdpp.nagisa.crowdpp2.util;

/**
 * Created by nagisa on 5/1/17.
 */

public class Sms {
    String name, body, date;
    public Sms(String name, String body, String date) {
        this.name = name;
        this.body = body;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public String getBody() {
        return body;
    }

    public String getDate() {
        return date;
    }
}
