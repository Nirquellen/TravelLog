package com.example.dragonmaster.knihajazd02.model;

import android.support.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Dragon Master on 23.3.2018.
 */

public class Log extends RealmObject /*implements Comparable<Log>*/ {
    @PrimaryKey
    public int id;
    @Required
    public Date date;
    public String start;
    public String end;
    public String distance;
}
