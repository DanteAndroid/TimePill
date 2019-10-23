package com.dante.diary.detail;

import android.os.Bundle;

import com.dante.diary.R;
import com.dante.diary.base.BaseActivity;
import com.dante.diary.base.Constants;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
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
