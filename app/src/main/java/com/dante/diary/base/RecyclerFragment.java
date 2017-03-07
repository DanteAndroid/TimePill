package com.dante.diary.base;


import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.dante.diary.R;
import com.dante.diary.utils.SpUtil;

import butterknife.BindView;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * All fragments have recyclerView & swipeRefresh must implement this.
 */
public abstract class RecyclerFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "RecyclerFragment";

    @BindView(R.id.list)
    public RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh)
    public SwipeRefreshLayout swipeRefresh;

    public boolean firstFetch;   //whether is first time to enter fragment
    public String imageType;               // imageType of recyclerView's content
    public int lastPosition;       //last visible position
    public int firstPosition;      //first visible position
    public Subscription subscription;
    public CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Override
    protected int initLayoutId() {
        return R.layout.fragment_recycler;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            //restoring position when reentering app.
            lastPosition = SpUtil.getInt(Constants.POSITION);
            if (lastPosition > 0) {
                log("restore lastPosition", lastPosition);
                recyclerView.scrollToPosition(lastPosition);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeSubscription.unsubscribe();
    }

    @Override
    protected void initViews() {
        recyclerView.setHasFixedSize(true);
        swipeRefresh.setColorSchemeColors(getColor(R.color.colorPrimary),
                getColor(R.color.colorPrimaryDark), getColor(R.color.colorAccent));
        Log.d(TAG, "initViews: setOnRefreshListener");
        swipeRefresh.setOnRefreshListener(this);
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onResume() {
        super.onResume();
        int viewPosition = SpUtil.getInt(Constants.VIEW_POSITION);
        if (viewPosition > 0) {
            recyclerView.scrollToPosition(viewPosition);
            SpUtil.remove(imageType + Constants.VIEW_POSITION);
        }

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
