package com.dante.diary.base;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

import com.dante.diary.R;
import com.dante.diary.follow.TabsFragment;
import com.dante.diary.login.LoginManager;
import com.dante.diary.main.MainDiaryFragment;
import com.dante.diary.profile.ProfileFragment;
import com.dante.diary.setting.SettingFragment;
import com.dante.diary.utils.SpUtil;
import com.ncapdevi.fragnav.FragNavController;
import com.roughike.bottombar.BottomBar;

import butterknife.BindView;
import rx.Subscription;

public class BottomBarActivity extends BaseActivity implements FragNavController.RootFragmentListener {
    private static final String TAG = "BottomBarActivity";

    private final int DIARIES = FragNavController.TAB1;
    private final int FOLLOWING = FragNavController.TAB2;
    private final int NOTIFICATION = FragNavController.TAB3;
    private final int ME = FragNavController.TAB4;
    public FragNavController controller;
    @BindView(R.id.bottomBar)
    public BottomBar bottomBar;
    public boolean isBarShown;
    View rootView;
    private int MAIN = FragNavController.TAB1;
    private boolean isHiding;
    private boolean isShowing;

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        if (SpUtil.getBoolean(SettingFragment.MY_HOME)) {
            MAIN = FragNavController.TAB4;
        }

        controller = new FragNavController(savedInstanceState, getSupportFragmentManager(), R.id.container, this, 4, MAIN);
        controller.setTransitionMode(FragmentTransaction.TRANSIT_NONE);
        initBottomBar();
        fetchNotifications();
    }

    public void fetchNotifications() {
        Subscription subscription = LoginManager.getApi().getTips().compose(applySchedulers())
                .subscribe(tipResults -> {
                    if (SpUtil.getBoolean("notifications_new_message", true)) {
                        bottomBar.getTabWithId(R.id.notification).setBadgeCount(tipResults.size());
                    }

                }, throwable -> Log.e("test", "fetch: " + throwable.getMessage()));
        getCompositeSubscription().add(subscription);
    }

    public void hideBottomBar() {
        if (isHiding) {
            return;
        }
        bottomBar.animate().translationY(bottomBar.getHeight())
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isHiding = false;
                        isBarShown = false;

                    }
                })
                .start();
        isHiding = true;
    }

    public void showBottomBar() {
        if (isShowing) {
            return;
        }
        bottomBar.animate().translationY(0)
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isShowing = false;
                        isBarShown = true;
                    }
                })
                .start();
        isShowing = true;
    }

    private void initBottomBar() {
        bottomBar.selectTabAtPosition(MAIN);
        bottomBar.setOnTabSelectListener(tabId -> {
            switch (tabId) {
                case R.id.main:
                    controller.switchTab(DIARIES);
                    break;
                case R.id.following:
                    controller.switchTab(FOLLOWING);
                    break;
                case R.id.notification:
                    controller.switchTab(NOTIFICATION);
                    bottomBar.getTabWithId(R.id.notification).setBadgeCount(0);
                    break;
                case R.id.me:
                    controller.switchTab(ME);
                    break;

            }
        });
        bottomBar.setOnTabReselectListener(tabId -> {
            controller.clearStack();
//            scrollToTop();
        });
    }

    private void scrollToTop() {
        Fragment fragment = controller.getCurrentFrag();
        if (fragment instanceof RecyclerFragment) {
            ((RecyclerFragment) fragment).scrollToTop();
        }
    }

    @Override
    protected int initLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public Fragment getRootFragment(int index) {
        switch (index) {
            case DIARIES:
                return MainDiaryFragment.newInstance(index);
            case FOLLOWING:
                return TabsFragment.newInstance(new String[]{getString(R.string.my_following_diary), getString(R.string.my_following)});
            case NOTIFICATION:
                return TabsFragment.newInstance(new String[]{getString(R.string.my_notifications), getString(R.string.my_followers)});
            case ME:
                int id = SpUtil.getInt(Constants.ID);
                return ProfileFragment.newInstance(id);
        }
        throw new IllegalStateException("Need to send an index that we know");
    }
}
