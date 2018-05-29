package com.example.dragonmaster.knihajazd02.adapter;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.dragonmaster.knihajazd02.fragment.FuelFragment;
import com.example.dragonmaster.knihajazd02.fragment.TravelFragment;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm)
    {
        super(fm);
    }

//    Fuj, nekonzistence v poloze zavorek
//    Fragment do importu
    @Override
    public android.support.v4.app.Fragment getItem(int index)
    {
        switch (index)
        {
            case 0:
                return new TravelFragment();
            case 1:
                return new FuelFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount()
    {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch(position){
            case 0:
//                strings.xml, mame radi preklady
                return "TRAVELS";
            case 1:
                return "GAS";
            default:
                return null;
        }
    }
}
