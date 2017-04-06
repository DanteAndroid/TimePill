package com.dante.diary.main;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.dante.diary.R;
import com.dante.diary.base.BottomBarActivity;
import com.dante.diary.custom.Updater;
import com.dante.diary.utils.UiUtils;

public class MainActivity extends BottomBarActivity {
    private static final String TAG = "MainActivity";
    private Updater updater;
    private boolean backPressed;

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

    private void doublePressBackToQuit() {
        if (backPressed) {
            super.onBackPressed();
            return;
        }
        backPressed = true;
        UiUtils.showSnack(getWindow().getDecorView(), R.string.leave_app);
        new Handler().postDelayed(() -> backPressed = false, 2000);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
        }else {
            doublePressBackToQuit();
        }
    }
}
