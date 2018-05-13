package com.example.dragonmaster.knihajazd02.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Fuel extends RealmObject {
    @PrimaryKey
    public int id;
    public Date date;
    public String amount;
}
