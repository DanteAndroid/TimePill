package com.dante.diary.base;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by yons on 17/3/17.
 */

public class TabPagerAdapter extends FragmentPagerAdapter {
    private List<RecyclerFragment> fragments;
    private String[] titles;

    public TabPagerAdapter(FragmentManager fm) {
        super(fm);
    }
    public void setFragments(List<RecyclerFragment> fragments, String[] titles) {
        this.fragments = fragments;
        this.titles = titles;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (titles != null) {
            return titles[position];
        }
        return null;
    }

}