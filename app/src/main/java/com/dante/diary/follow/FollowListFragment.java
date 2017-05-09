package com.dante.diary.follow;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.dante.diary.R;
import com.dante.diary.base.Constants;
import com.dante.diary.base.RecyclerFragment;
import com.dante.diary.login.LoginManager;
import com.dante.diary.model.User;
import com.dante.diary.net.TimeApi;
import com.dante.diary.utils.UiUtils;

import java.io.IOException;
import java.util.List;

import rx.Observable;

/**
 * A simple {@link Fragment} subclass.
 */
public class FollowListFragment extends RecyclerFragment {
    public static final String FOLLOWING = "following";
    public static final String FOLLOWER = "follower";
    private static final int FETCH_FOLLOW_SIZE = 20;
    FollowListAdapter adapter;
    String type;
    private LinearLayoutManager layoutManager;
    private int page = 1;

    public static FollowListFragment newInstance(String type) {
        Bundle args = new Bundle();
        args.putString(Constants.TYPE, type);
        FollowListFragment fragment = new FollowListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initViews() {
        super.initViews();
        adapter = new FollowListAdapter(null);
        adapter.setEmptyView(R.layout.empty_following, (ViewGroup) rootView);
        adapter.disableLoadMoreIfNotFullPage(recyclerView);
        layoutManager = new LinearLayoutManager(barActivity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        type = getArguments().getString(Constants.TYPE);

        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                goProfile(adapter.getItem(i).getId());
            }

            @Override
            public void onItemLongClick(BaseQuickAdapter baseQuickAdapter, View view, int position) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(type.equals(FOLLOWING) ? R.string.unfollow_message : R.string.cancel_followed_message)
                        .setPositiveButton(R.string.cancel_followed, (dialog, which) -> {
                            if (type.equals(FOLLOWING)) {
                                unFollow(position);
                            } else {
                                cancelFollowed(position);
                            }
                        })
                        .setNegativeButton(R.string.nope, null).show();

            }
        });
        fetch();
    }

    private void unFollow(int position) {
        subscription = LoginManager.getApi().unfollow(adapter.getItem(position).getId())
                .compose(applySchedulers())
                .subscribe(responseBodyResponse -> {
                    adapter.notifyItemRemoved(position);
                    UiUtils.showSnack(rootView, getString(R.string.unfollow_success));
                });
        compositeSubscription.add(subscription);
    }

    private void cancelFollowed(int position) {
        int id = adapter.getItem(position).getId();
        subscription = LoginManager.getApi().cancelFollowed(id)
                .compose(applySchedulers())
                .subscribe(responseBodyResponse -> {
                    adapter.notifyItemRemoved(position);
                    try {
                        log("cancel followed" + responseBodyResponse.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, throwable -> UiUtils.showSnack(rootView, getString(R.string.cancel_followed_failed) + " " + throwable.getMessage()));
    }

    @Override
    protected boolean hasFab() {
        return false;
    }


    @Override
    protected void initData() {
        super.initData();
        adapter.setOnLoadMoreListener(() -> fetch(), recyclerView);
    }

    protected void fetch() {
        changeRefresh(true);

        subscription = followSource()
                .map(listUsersResult -> listUsersResult.users).compose(applySchedulers())
                .subscribe(users -> {
                    if (users.isEmpty()) {
                        adapter.loadMoreEnd();
                    } else {
                        if (page == 1) {
                            adapter.setNewData(users);
                        } else {
                            adapter.addData(users);
                            adapter.loadMoreComplete();
                        }
                        page++;
                    }
                    changeRefresh(false);

                }, throwable -> {
                    changeRefresh(false);
                    UiUtils.showSnack(rootView, getString(R.string.cant_get_following) + throwable.getMessage());
                });

    }

    private Observable<TimeApi.UsersResult<List<User>>> followSource() {
        if (type.equals(FOLLOWER)) {
            return LoginManager.getApi().getMyFollowers(page, FETCH_FOLLOW_SIZE);
        }

        return LoginManager.getApi()
                .getFollowings(page, FETCH_FOLLOW_SIZE);
    }

    @Override
    public void onRefresh() {
        page = 1;
        fetch();
    }
}
