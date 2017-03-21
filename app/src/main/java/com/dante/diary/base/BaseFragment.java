package com.dante.diary.base;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Transition;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dante.diary.BuildConfig;
import com.dante.diary.R;
import com.dante.diary.profile.ProfileFragment;

import butterknife.ButterKnife;
import io.realm.Realm;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * BaseFragment helps onCreateView, and initViews(when root is null), init data on Activity Created.
 */
public abstract class BaseFragment extends Fragment {
    private static final String TAG = "BaseFragment";
    public CompositeSubscription compositeSubscription = new CompositeSubscription();
    public Subscription subscription;
    protected View rootView;
    protected Realm realm;
    protected Toolbar toolbar;
    protected BottomBarActivity activity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setAnimations();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(initLayoutId(), container, false);
            ButterKnife.bind(this, rootView);
            initViews();
        }
        onCreateView();
        return rootView;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void setAnimations() {
//        setEnterTransition(initTransitions());
//        setExitTransition(initTransitions());
//        setReturnTransition(initTransitions());
//        setReenterTransition(initTransitions());
//        setAllowEnterTransitionOverlap(isTransitionAllowOverlap());
//        setAllowReturnTransitionOverlap(isTransitionAllowOverlap());
        postponeEnterTransition();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected Transition initTransitions() {
        return new Fade();
    }

    protected boolean isTransitionAllowOverlap() {
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isHidden", isHidden());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof BottomBarActivity) {
            activity = (BottomBarActivity) getActivity();
        }
        initAppBar();
        initData();
    }

    @Override
    public void onDestroyView() {
        if (compositeSubscription.hasSubscriptions()) {
            compositeSubscription.clear();
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        App.getWatcher(getActivity()).watch(this);

    }


    protected abstract int initLayoutId();

    protected void onCreateView() {
        ButterKnife.bind(this, rootView);
        realm = ((BaseActivity) getActivity()).realm;
        toolbar = ((BaseActivity) getActivity()).toolbar;
    }

    public void initAppBar() {
        toolbar = (Toolbar) getView().findViewById(R.id.toolbar);
        if (getActivity() != null && null != toolbar) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(needNavigation());
            toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        }
        toggleBottomBar();
    }

    public void toggleBottomBar() {
        if (activity != null) {
            if (getStackCount() == 0) {
                activity.showBottomBar();
            } else {
                activity.hideBottomBar();
            }
        }
    }

    public int getStackCount() {
        return getActivity().getSupportFragmentManager().getBackStackEntryCount();
    }

    protected boolean needNavigation() {
        return true;
    }

    protected abstract void initViews();

    protected abstract void initData();

    public <T> Observable.Transformer<T, T> applySchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .distinct();
    }


    public void log(String key, String content) {
        if (BuildConfig.DEBUG && getUserVisibleHint()) {
            Log.d(getClass().getSimpleName(), key + "  " + content);
        }
    }

    public void log(String key, int content) {
        log(key, String.valueOf(content));
    }

    public void log(String key) {
        log(key, "");
    }

    public void goProfile(int userId) {
        Fragment f = ProfileFragment.newInstance(userId);
        add(f);
    }


    public void add(Fragment f) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right
                        , android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .hide(this)
                .add(R.id.container, f)
                .addToBackStack("")
                .commit();
    }

    public void replace(Fragment f) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right
                        , android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.container, f)
                .addToBackStack("")
                .commit();

    }

    public void setScrollFlag(boolean scrollable) {
        AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        params.setScrollFlags(scrollable ? AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS : 0);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        toggleBottomBar();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint: " + isVisibleToUser + " " + getClass().getName());
    }

    public void finishFragment() {
        if (activity != null) {
            activity.controller.popFragment();
        }
    }

    public void pushFragment(Fragment f) {
        if (activity != null) {
            activity.controller.pushFragment(f);
        }
    }
}
