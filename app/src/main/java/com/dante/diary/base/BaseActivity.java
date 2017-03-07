package com.dante.diary.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.dante.diary.R;

import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * BaseActivity includes a base layoutId, init its toolbar (if the layout has one)
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    public Realm realm;
    public Toolbar toolbar;
    private boolean isShowToolbar = true;
    private Fragment currentFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews(savedInstanceState);
    }

    protected abstract int initLayoutId();

    /**
     * MUST override and call the SUPER method
     */
    protected void initViews(@Nullable Bundle savedInstanceState) {
        setContentView(initLayoutId());
        ButterKnife.bind(this);
        initAppBar();
        initSDK();
    }

    private void initSDK() {
        realm = Realm.getDefaultInstance();

    }


    public void initAppBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (null != toolbar) {
            setSupportActionBar(toolbar);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(needNavigation());
        }
    }

    protected boolean needNavigation(){
        return true;
    }

    public void toggleToolbar() {
        if (isShowToolbar) {
            hideToolbar();
        } else {
            showToolbar();
        }
    }

    public void hideToolbar() {
        if (toolbar != null) {
            isShowToolbar = false;
            toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
        }
    }

    public void showToolbar() {
        if (toolbar != null) {
            isShowToolbar = true;
            toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    public void setToolbarTitle(String title) {
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        Bugtags.onDispatchTouchEvent(this, ev);
//        return super.dispatchTouchEvent(ev);
//    }

}
