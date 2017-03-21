package com.dante.diary.main;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dante.diary.base.BottomBarActivity;

public class MainActivity extends BottomBarActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
    }

    @Override
    protected boolean needNavigation() {
        return false;
    }


    private void initFab() {

    }

//    @Override
//    public void onBackPressed() {
//        Log.d(TAG, "onBackPressed: " + controller.getSize());
//        if (controller.isRootFragment()) {
//            Log.d(TAG, "onBackPressed: "+"isRoot");
//            super.onBackPressed();
//        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
//            Log.d(TAG, "onBackPressed: "+"popBackStack");
//            getSupportFragmentManager().popBackStack();
//
//        } else {
//            Log.d(TAG, "onBackPressed: "+"popBackStack");
//            controller.popFragment();
//        }
//
//    }
}
