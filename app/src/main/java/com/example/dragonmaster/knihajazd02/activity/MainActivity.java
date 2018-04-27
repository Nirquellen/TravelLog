package com.example.dragonmaster.knihajazd02.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.dragonmaster.knihajazd02.R;
import com.example.dragonmaster.knihajazd02.adapter.TabsPagerAdapter;
import com.example.dragonmaster.knihajazd02.model.Fuel;
import com.example.dragonmaster.knihajazd02.model.Log;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity{

    ViewPager mViewPager;
    TabLayout mTabLayout;
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = findViewById(R.id.viewpager);
        mViewPager.setAdapter(new TabsPagerAdapter(getSupportFragmentManager()));

        mTabLayout = findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.export:
                export();
        }
        return super.onOptionsItemSelected(item);
    }

    private void export() {
        int km = counting();
        if(km != 0) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage(getResources().getString(R.string.km_left, km))
                    .setCancelable(true).setNeutralButton(R.string.okay,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    }).show();
        } else {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            makePdf();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            dialog.cancel();
                            break;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.pdf_export_question).setPositiveButton(R.string.yes, dialogClickListener)
                    .setNegativeButton(R.string.no, dialogClickListener).show();
        }
    }

    private int counting() {
        float consumption = Float.valueOf(mSharedPreferences.getString("consumption", null));
        android.util.Log.d("Main Activity", String.valueOf(consumption));
        float km = 0;
        float fuel = 0;
        Realm mRealm = Realm.getDefaultInstance();
        RealmResults<Log> logs = mRealm.where(Log.class).findAll();
        for(Log log : logs) {
            km += Float.valueOf(log.distance.replaceAll("[^0-9.]", ""));
        }
        RealmResults<Fuel> fuels = mRealm.where(Fuel.class).findAll();
        for(Fuel f : fuels) {
            fuel += Float.valueOf(f.amount);
        }
        return (int)(fuel/consumption*100 - km);
    }

    private void makePdf() {

    }
}