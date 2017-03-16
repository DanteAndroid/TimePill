package com.dante.diary.profile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.dante.diary.R;
import com.dante.diary.base.Constants;
import com.dante.diary.base.RecyclerFragment;
import com.dante.diary.login.LoginManager;
import com.dante.diary.model.DataBase;
import com.dante.diary.model.Notebook;
import com.dante.diary.utils.UiUtils;

import butterknife.BindView;
import io.realm.RealmResults;

/**
 * Created by yons on 17/3/9.
 */

public class NoteBookListFragment extends RecyclerFragment {
    private static final String TAG = "DiaryListFragment";
    StaggeredGridLayoutManager layoutManager;
    NotebookListAdapter adapter;
    RealmResults<Notebook> notebooks;
    @BindView(R.id.stateText)
    TextView stateText;

    private int page;
    private int userId;

    public static NoteBookListFragment newInstance(int userId) {

        Bundle args = new Bundle();
        args.putInt(Constants.ID, userId);
        NoteBookListFragment fragment = new NoteBookListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initViews() {
        super.initViews();
        adapter = new NotebookListAdapter(null);
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                onNotebookClicked(view, i);
            }
        });

    }

    private void onNotebookClicked(View view, int index) {
        int userId = adapter.getItem(index).getId();
        Fragment f = DiaryListFragment.newInstance(userId, adapter.getItem(index).getSubject());
        activity.controller.pushFragment(f);
    }

    @Override
    protected void initData() {
        super.initData();
        userId = getArguments().getInt(Constants.ID);
        if (userId <= 0) {
            UiUtils.showSnack(rootView, R.string.get_profile_failed);
            return;
        }

        notebooks = DataBase.findNotebooks(realm, userId);
        adapter.setNewData(notebooks);
        if (notebooks.isEmpty()) {
            fetch();
        }
    }

    private void fetch() {
        changeRefresh(true);

        subscription = LoginManager.getApi()
                .getMyNotebooks(userId)
                .compose(applySchedulers())
                .subscribe(notebooks -> {
                    if (page <= 1 && notebooks.isEmpty()) {
                        stateText.setVisibility(View.VISIBLE);
                        Log.d(TAG, "call: notebooks are empty");
                    } else {
                        adapter.notifyItemRangeChanged(0, notebooks.size());
                        Log.d(TAG, "call: setNewData");
                    }
                    DataBase.save(realm, notebooks);
                    changeRefresh(false);
                }, Throwable::printStackTrace);
    }

    @Override
    public void onRefresh() {
        fetch();
    }
}
