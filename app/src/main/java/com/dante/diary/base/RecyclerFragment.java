package com.dante.diary.base;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.annotation.CallSuper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.dante.diary.R;
import com.dante.diary.utils.SpUtil;

import butterknife.BindView;

/**
 * All fragments have recyclerView & swipeRefresh must implement this.
 */
public abstract class RecyclerFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    public static final int SMOOTH_SCROLL_POSITION = 40;
    private static final String TAG = "RecyclerFragment";
    @BindView(R.id.list)
    public RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh)
    public SwipeRefreshLayout swipeRefresh;
    public RecyclerView.LayoutManager layoutManager;
    public boolean firstEnter;   //whether is first time to enter fragment
    @BindView(R.id.fab)
    public FloatingActionButton fab;

    @Override
    protected int initLayoutId() {
        return R.layout.fragment_recycler;
    }

//    @Override
//    protected void setAnimations() {
//        setReturnTransition(initTransitions());
//        setReenterTransition(new Slide(Gravity.RIGHT));
//        setEnterTransition(new Slide(Gravity.RIGHT));
//        setExitTransition(initTransitions());
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @CallSuper
    @Override
    protected void initViews() {
        recyclerView.setHasFixedSize(true);
        swipeRefresh.setColorSchemeColors(getColor(R.color.colorPrimary),
                getColor(R.color.colorPrimaryDark), getColor(R.color.colorAccent));
        swipeRefresh.setOnRefreshListener(this);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 30) {
                    if (activity != null) {
                        activity.hideBottomBar(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (fab != null && fab.isShown()) {
                                    fab.postDelayed(() -> fab.hide(), 200);
                                }
                            }
                        });
                    }

                }
                if (dy < -60) {
                    if (getStackCount() == 0) {
                        activity.showBottomBar(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (fab != null && !fab.isShown() && hasFab()) {
                                    fab.postDelayed(() -> fab.show(), 200);
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    protected abstract boolean hasFab();

    @Override
    protected void initData() {

    }

    protected abstract void fetch();

    @Override
    public void onResume() {
        super.onResume();
        int viewPosition = SpUtil.getInt(Constants.VIEW_POSITION);

        if (viewPosition > 0) {
            recyclerView.scrollToPosition(viewPosition);
            SpUtil.remove(Constants.VIEW_POSITION);
        }
    }


    public void changeRefresh(final boolean refreshState) {
        if (null != swipeRefresh) {
            swipeRefresh.setRefreshing(refreshState);
            if (!refreshState) {
                startPostponedEnterTransition();
            }
        }
    }

    public int getColor(int resId) {
        return ResourcesCompat.getColor(getResources(), resId, null);
    }

    public void scrollToTop() {
        if (layoutManager instanceof LinearLayoutManager) {
            if (((LinearLayoutManager) layoutManager).findLastVisibleItemPosition() > SMOOTH_SCROLL_POSITION) {
                recyclerView.scrollToPosition(0);
            } else {
                recyclerView.smoothScrollToPosition(0);
            }
        }
    }

}
