package com.dante.diary.notification;

import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.dante.diary.R;
import com.dante.diary.base.RecyclerFragment;
import com.dante.diary.detail.DiaryDetailFragment;
import com.dante.diary.login.LoginManager;
import com.dante.diary.model.TipResult;
import com.dante.diary.utils.UiUtils;

import java.io.IOException;
import java.util.List;

/**
 * Created by yons on 17/3/17.
 */

public class NotificationListFragment extends RecyclerFragment implements com.dante.diary.interfaces.OnItemClickListener {

    private LinearLayoutManager layoutManager;
    private NotificationListAdapter adapter;
    private String data;
    private int type;

    @Override
    protected void initViews() {
        super.initViews();
        adapter = new NotificationListAdapter(null, this);
        adapter.setEmptyView(R.layout.empty_notification, (ViewGroup) rootView);
        layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                onNotificationClicked(view, i);

            }

            @Override
            public void onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                log("onItemLongClick~~~~~~~~~~~~~~");
            }

            @Override
            public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int position) {
                int id = view.getId();
                if (id == R.id.done) {
                    readDone(adapter.getItem(position).id);
                    adapter.remove(position);
                }

            }
        });
        fab.setImageResource(R.drawable.ic_done_all_white_36dp);
        fab.setOnClickListener(v -> readAllDone());
    }

    @Override
    protected boolean hasFab() {
        return true;
    }

    private void readDone(int id) {
        LoginManager.getApi()
                .tipsRead(String.valueOf(id))
                .compose(applySchedulers())
                .subscribe(responseBodyResponse -> {
                    try {
                        log("" + responseBodyResponse.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, throwable -> {
                    UiUtils.showSnack(rootView, getString(R.string.cant_connect_net));
                    throwable.printStackTrace();
                });
    }

    private void readAllDone() {
        List<TipResult> results = adapter.getData();
        if (results.isEmpty()) {
            UiUtils.showSnack(rootView, getString(R.string.no_new_notification));
            fab.hide();
            return;
        }

        StringBuilder ids = new StringBuilder();
        for (TipResult r : results) {
            ids.append(r.id).append(",");
        }
        log(" ids: " + ids.toString());

        LoginManager.getApi()
                .tipsRead(ids.toString())
                .compose(applySchedulers())
                .subscribe(responseBodyResponse -> {
                    UiUtils.showSnack(rootView, "已全部标为已读");
                    fab.hide();

                    try {
                        log("" + responseBodyResponse.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, throwable -> {
                    UiUtils.showSnack(rootView, getString(R.string.cant_connect_net));
                    throwable.printStackTrace();
                });
    }

    @Override
    protected void initData() {
        super.initData();
        fetch();
    }

    protected void fetch() {
        changeRefresh(true);

        LoginManager.getApi().getTips().compose(applySchedulers())
                .subscribe(tipResults -> {
                    if (tipResults.isEmpty()) {
                        fab.hide();
                    } else {
                        fab.show();
                    }
                    adapter.setNewData(tipResults);
                    changeRefresh(false);
                }, throwable -> {
                    UiUtils.showSnack(rootView, getString(R.string.cant_get_notifications));
                    changeRefresh(false);
                    throwable.printStackTrace();
                });
    }

    private void onNotificationClicked(View view, int i) {
        TipResult notification = adapter.getItem(i);
        int type = adapter.getItem(i).getItemType();
        if (type == TipResult.TYPE_FOLLOW) {
            goProfile(notification.content.getFollowUser().getId());

        } else if (type == TipResult.TYPE_COMMENT) {
            Fragment fragment = DiaryDetailFragment.newInstance(notification.content.getDairyId(),
                    notification.content.getCommentId());
            add(fragment);
        }

//        readDone(notification.id);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        fetch();
    }

    @Override
    public void onRefresh() {
        fetch();
    }

    @Override
    public void onItemClick(int i) {
        goProfile(i);
    }

    @Override
    public void onItemLongClick(int position) {

    }
}
