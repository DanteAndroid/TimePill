package com.dante.diary.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.dante.diary.R;
import com.dante.diary.detail.ViewerActivity;
import com.dante.diary.follow.FollowFragment;
import com.dante.diary.login.LoginFragment;
import com.dante.diary.main.MainDiaryFragment;
import com.dante.diary.profile.ProfileFragment;
import com.ncapdevi.fragnav.FragNavController;
import com.roughike.bottombar.BottomBar;

import butterknife.BindView;

import static com.dante.diary.base.App.context;

public class BottomBarActivity extends BaseActivity implements FragNavController.RootFragmentListener {
    private final int MAIN = FragNavController.TAB1;
    private final int FOLLOWING = FragNavController.TAB2;
    private final int NOTIFICATION = FragNavController.TAB3;
    private final int ME = FragNavController.TAB4;

    public FragNavController controller;
    @BindView(R.id.bottomBar)
    public BottomBar bottomBar;
    @BindView(R.id.container)
    FrameLayout container;

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        controller = new FragNavController(savedInstanceState, getSupportFragmentManager(), R.id.container, this, 4, MAIN);
        controller.setTransitionMode(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        initBottomBar();

    }

    public void hideBottomBar() {
        bottomBar.animate().translationY(bottomBar.getHeight()).setInterpolator(new AccelerateInterpolator()).start();
    }

    public void showBottomBar() {
        bottomBar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
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
                    controller.switchTab(NOTIFICATION);
                    break;
                case R.id.me:
                    controller.switchTab(ME);
                    break;

            }
        });
        bottomBar.setOnTabReselectListener(tabId -> {
            controller.clearStack();
            scrollToTop();
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
                return FollowFragment.newInstance();
            case NOTIFICATION:
                return new ProfileFragment();
            case ME:
                return new LoginFragment();
        }
        throw new IllegalStateException("Need to send an index that we know");
    }

    public void startViewer(View view, String url) {
        ViewCompat.setTransitionName(view, url);
        Intent intent = new Intent(context, ViewerActivity.class);
        intent.putExtra(Constants.URL, url);
        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this, view, url);
        ActivityCompat.startActivity(this, intent, options.toBundle());
    }
}
