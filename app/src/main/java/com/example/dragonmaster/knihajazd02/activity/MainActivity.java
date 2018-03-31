package com.example.dragonmaster.knihajazd02.activity;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.dragonmaster.knihajazd02.R;
import com.example.dragonmaster.knihajazd02.fragment.MainFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager != null) {
                fragmentManager.beginTransaction()
                        .replace(android.R.id.content,
                                MainFragment.newInstance(),
                                MainFragment.class.getSimpleName())
                        .commit();
            }
        }
    }
}