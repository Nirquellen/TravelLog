package com.example.dragonmaster.knihajazd02;

import android.app.Application;
import io.realm.Realm;

/**
 * Created by Dragon Master on 25.3.2018.
 */

public class App extends Application {
    private static App sInstance;

    public static App getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        Realm.init(this);
    }
}
