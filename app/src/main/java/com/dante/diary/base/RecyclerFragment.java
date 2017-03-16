package com.dante.diary.base;


import android.support.annotation.CallSuper;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;

import com.dante.diary.R;

import butterknife.BindView;

/**
 * All fragments have recyclerView & swipeRefresh must implement this.
 */
public abstract class RecyclerFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "RecyclerFragment";

    @BindView(R.id.list)
    public RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh)
    public SwipeRefreshLayout swipeRefresh;

    public boolean firstEnter;   //whether is first time to enter fragment

    @Override
    protected int initLayoutId() {
        return R.layout.fragment_recycler;
    }

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
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public void changeRefresh(final boolean refreshState) {
        if (null != swipeRefresh) {
            swipeRefresh.setRefreshing(refreshState);
        }
    }

    public int getColor(int resId) {
        return ResourcesCompat.getColor(getResources(), resId, null);
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }


}
