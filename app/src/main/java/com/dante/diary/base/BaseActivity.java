package com.dante.diary.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.bugtags.library.Bugtags;
import com.dante.diary.R;
import com.dante.diary.model.DataBase;

import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * BaseActivity includes a base layoutId, init its toolbar (if the layout has one)
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    public DataBase base;
    public Toolbar toolbar;
    private boolean isShowToolbar = true;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews(savedInstanceState);
    }

    protected abstract int initLayoutId();

    @CallSuper
    protected void initViews(@Nullable Bundle savedInstanceState) {
        setContentView(initLayoutId());
        ButterKnife.bind(this);
        initAppBar();
        initSDK();
    }

    private void initSDK() {
        base = DataBase.getInstance();
    }


    public void initAppBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (null != toolbar) {
            setSupportActionBar(toolbar);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(needNavigation());
            if (needNavigation() && toolbar != null) {
                toolbar.setNavigationOnClickListener(v -> onBackPressed());
            }
        }
    }

    protected boolean needNavigation() {
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
        base.close();
    }

    public void setToolbarTitle(String title) {
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    public <T> Observable.Transformer<T, T> applySchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .distinct();
    }

    public void showProgress() {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
        }
        dialog.show();
    }

    public void hideProgress() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Bugtags.onDispatchTouchEvent(this, ev);
        return super.dispatchTouchEvent(ev);
    }

}
