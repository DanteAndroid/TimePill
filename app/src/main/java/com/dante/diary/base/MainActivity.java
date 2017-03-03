package com.dante.diary.base;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dante.diary.main.DiaryFragment;
import com.dante.diary.main.dummy.DummyContent;

public class MainActivity extends BaseControllerActivity implements DiaryFragment.OnInteractListener {

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
    }

    @Override
    protected boolean needNavigation() {
        return false;
    }

    @Override
    public void onInteract(DummyContent.DummyItem item) {

    }


    private void initFab() {

    }

}
