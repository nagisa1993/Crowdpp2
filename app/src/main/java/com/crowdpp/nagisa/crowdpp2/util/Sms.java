package com.crowdpp.nagisa.crowdpp2.util;

/**
 * Created by nagisa on 5/1/17.
 */

public class Sms {
    String name, date, smstype;
    public Sms(String name, String date, String smstype) {
        this.name = name;
        this.date = date;
        this.smstype = smstype;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getSmstype() {return  smstype;}
}
