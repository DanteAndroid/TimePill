package com.dante.diary.main;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dante.diary.base.BottomBarActivity;
import com.dante.diary.custom.Updater;

public class MainActivity extends BottomBarActivity {
    private static final String TAG = "MainActivity";
    private Updater updater;

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        initUpdater();
    }

    @Override
    protected boolean needNavigation() {
        return false;
    }


    private void initUpdater() {
        updater = Updater.getInstance(this);
        updater.check();
    }
    @Override
    protected void onDestroy() {
        updater.release();
        super.onDestroy();
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
