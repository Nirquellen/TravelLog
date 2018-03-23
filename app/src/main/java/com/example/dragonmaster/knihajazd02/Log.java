package com.example.dragonmaster.knihajazd02;

import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Created by Dragon Master on 23.3.2018.
 */

public class Log implements Comparable<Log> {
    public String date;
    public String start;
    public String end;
    public String distance;

    @Override
    public int compareTo(@NonNull Log log) {
        return date.compareTo(log.date);
    }
}
