package com.dante.diary.main;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dante.diary.R;
import com.dante.diary.base.BottomBarActivity;
import com.dante.diary.base.EventMessage;
import com.dante.diary.custom.Updater;
import com.dante.diary.utils.AppUtil;
import com.dante.diary.utils.SpUtil;
import com.dante.diary.utils.UiUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends BottomBarActivity {
    private static final String TAG = "MainActivity";
    private Updater updater;
    private boolean backPressed;

    public static void initUI() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        if (SpUtil.getBoolean("auto_night_mode")) {
            AppUtil.autoNightMode();
        }
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        initUI();
        initUpdater();
        EventBus.getDefault().register(this);
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
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void doublePressBackToQuit() {
        if (!isBarShown) {
            showBottomBar();
            return;
        }

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
        } else {
            super.onBackPressed();
//            doublePressBackToQuit();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(EventMessage message) {
        String event = message.event;
        switch (event) {
            case "restart":
                finish();
                break;
            case "invalidateOptionsMenu":
                invalidateOptionsMenu();
                break;
            default:
                Log.d(TAG, "onMessage: " + event);
                break;
        }
    }
}
