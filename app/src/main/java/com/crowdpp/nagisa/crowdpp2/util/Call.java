package com.crowdpp.nagisa.crowdpp2.util;

/**
 * Created by nagisa on 5/1/17.
 */

public class Call {
    String name, date, duration, callerType;
    public Call(String name, String date, String duration, String callerType) {
        this.name = name;
        this.date = date;
        this.duration = duration;
        this.callerType = callerType;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getDuration() {
        return duration;
    }

    public String getCallerType() { return callerType; }
}
