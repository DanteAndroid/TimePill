package com.dante.diary.main;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dante.diary.base.BaseControllerActivity;

public class MainActivity extends BaseControllerActivity {

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

}
