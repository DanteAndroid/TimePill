package com.dante.diary.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.dante.diary.R;
import com.dante.diary.base.BaseActivity;
import com.dante.diary.base.Constants;

public class ViewerActivity extends BaseActivity {
    private static final String TAG = "ViewerActivity";

    private String url;

    @Override
    protected int initLayoutId() {
        return R.layout.framelayout;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        supportPostponeEnterTransition();

        url = getIntent().getStringExtra(Constants.URL);
        Fragment fragment = ViewerFragment.newInstance(url);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .commit();

    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }

}
