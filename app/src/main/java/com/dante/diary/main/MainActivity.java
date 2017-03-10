package com.dante.diary.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dante.diary.base.BaseControllerActivity;

public class MainActivity extends BaseControllerActivity {
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

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: " + controller.getSize());
        if (controller.isRootFragment()) {
            super.onBackPressed();
        } else {
            controller.popFragment();
        }

    }
}
