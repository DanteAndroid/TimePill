package com.dante.diary.base;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.widget.FrameLayout;

import com.dante.diary.R;
import com.dante.diary.login.LoginFragment;
import com.dante.diary.main.MainDiaryFragment;
import com.dante.diary.profile.ProfileFragment;
import com.ncapdevi.fragnav.FragNavController;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;

import butterknife.BindView;

public class BaseControllerActivity extends BaseActivity implements FragNavController.RootFragmentListener {
    private final int MAIN = FragNavController.TAB1;
    private final int FOLLOWING = FragNavController.TAB2;
    private final int OTHER = FragNavController.TAB3;
    private final int ME = FragNavController.TAB4;
    public FragNavController controller;
    @BindView(R.id.container)
    FrameLayout container;
    @BindView(R.id.bottomBar)
    BottomBar bottomBar;

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        controller = new FragNavController(savedInstanceState, getSupportFragmentManager(), R.id.container, this, 4, MAIN);
        initBottomBar();

    }

    private void initBottomBar() {
        bottomBar.selectTabAtPosition(MAIN);
        bottomBar.setOnTabSelectListener(tabId -> {
            switch (tabId) {
                case R.id.main:
                    controller.switchTab(MAIN);
                    break;
                case R.id.following:
                    controller.switchTab(FOLLOWING);
                    break;
                case R.id.other:
                    controller.switchTab(OTHER);
                    break;
                case R.id.me:
                    controller.switchTab(ME);
                    break;

            }
        });
        bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                controller.clearStack();
                scrollToTop();
            }
        });
    }

    private void scrollToTop() {
        Fragment fragment = controller.getCurrentFrag();
        if (fragment instanceof MainDiaryFragment) {
            ((MainDiaryFragment) fragment).getRecyclerView().smoothScrollToPosition(0);
        }
    }

    @Override
    protected int initLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public Fragment getRootFragment(int index) {
        switch (index) {
            case MAIN:
                return MainDiaryFragment.newInstance(index);
            case FOLLOWING:
                return MainDiaryFragment.newInstance(index);
            case OTHER:
                return new ProfileFragment();
            case ME:
                return new LoginFragment();
        }
        throw new IllegalStateException("Need to send an index that we know");
    }
}
