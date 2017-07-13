package com.dante.diary.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;

import com.dante.diary.R;
import com.dante.diary.base.BaseActivity;
import com.dante.diary.base.Constants;

import butterknife.BindView;

public class PictureActivity extends BaseActivity {
    private static final String TAG = "ViewerActivity";
    @BindView(R.id.frame_layout)
    CoordinatorLayout frameLayout;

    private String url;
    private boolean isGif;

    @Override
    protected int initLayoutId() {
        return R.layout.framelayout;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        supportPostponeEnterTransition();
        frameLayout.setFitsSystemWindows(false);
        url = getIntent().getStringExtra(Constants.URL);
        isGif = getIntent().getBooleanExtra("isGif", false);
        Fragment fragment = PictureFragment.newInstance(url, isGif);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .commit();

    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }

}
