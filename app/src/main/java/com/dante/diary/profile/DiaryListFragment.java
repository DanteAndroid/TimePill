package com.dante.diary.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.dante.diary.R;
import com.dante.diary.base.Constants;
import com.dante.diary.base.RecyclerFragment;
import com.dante.diary.detail.DiariesViewerActivity;
import com.dante.diary.detail.DiaryDetailFragment;
import com.dante.diary.login.LoginManager;
import com.dante.diary.main.DiaryListAdapter;
import com.dante.diary.model.Diary;
import com.dante.diary.utils.SpUtil;

import java.util.List;

import butterknife.BindView;
import rx.Observable;

/**
 * Created by yons on 17/3/9.
 */

public class DiaryListFragment extends RecyclerFragment {
    public static final int DIARY_LIST_TYPE_USER = 0;
    public static final int DIARY_LIST_TYPE_NOTEBOOK = 1;
    public static final String FOLLOWING = "following";
    private static final String TAG = "DiaryListFragment";
    DiaryListAdapter adapter;
    Observable<List<Diary>> diaries;
    @BindView(R.id.stateText)
    TextView stateText;

    private int page = 1;
    private int id;
    private String subject;
    private boolean isFromNotebook;

    //id可以是用户id，也可以是notebook的id
    public static DiaryListFragment newInstance(int id, String notebookSubject) {
        Bundle args = new Bundle();
        args.putInt(Constants.ID, id);
        args.putString(Constants.DATA, notebookSubject);
        DiaryListFragment fragment = new DiaryListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int initLayoutId() {
        return R.layout.fragment_diary_main;
    }


    @Override
    protected void initViews() {
        super.initViews();
        adapter = new DiaryListAdapter(null);
        layoutManager = new LinearLayoutManager(barActivity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                onDiaryClicked(view, i);
            }

            @Override
            public void onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                log("onSimpleItemClick~~~~~~~~~~~~~~");
            }

            @Override
            public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                int id = view.getId();
                if (id == R.id.avatar) {
                    goProfile(adapter.getItem(i).getUserId());
                }

            }
        });
    }

    @Override
    protected boolean hasFab() {
        return false;
    }

    private void onDiaryClicked(View view, int i) {
        if (isFromNotebook) {
            Intent intent = new Intent(getContext().getApplicationContext(), DiariesViewerActivity.class);
            intent.putExtra(Constants.POSITION, i);
            intent.putExtra(Constants.NOTEBOOK_ID, adapter.getItem(i).getNotebookId());
            startActivity(intent);

        } else {
            int diaryId = adapter.getItem(i).getId();
            Fragment f = DiaryDetailFragment.newInstance(diaryId);
            add(f);
        }
    }

    @Override
    protected void initData() {
        super.initData();
        if (getArguments() != null) {
            //有参数则获取参数id
            id = getArguments().getInt(Constants.ID);
            log("gogo" + id);
            subject = getArguments().getString(Constants.DATA);
        } else {
            //我的日记列表
            id = SpUtil.getInt(Constants.ID);
        }
        if (!TextUtils.isEmpty(subject) && !subject.equals(FOLLOWING)) {
            isFromNotebook = true;
            toolbar.setVisibility(View.VISIBLE);
            toolbar.setTitle(subject);
        }

        fetch();
    }

    protected void fetch() {
        changeRefresh(true);

        subscription = diariesSource()
                .compose(applySchedulers())
                .subscribe(diaries -> {
                    if (isFromNotebook) {
                        base.save(diaries);
                    }

                    if (page <= 1 && diaries.isEmpty()) {
                        stateText.setText(R.string.no_today_diary);
                        stateText.setVisibility(View.VISIBLE);
                    } else {
                        adapter.setNewData(diaries);
                        page++;
                    }

                    changeRefresh(false);
                }, Throwable::printStackTrace);
    }

    private Observable<List<Diary>> diariesSource() {
        if (!TextUtils.isEmpty(subject)) {
            if (subject.equals(FOLLOWING)) {
                return LoginManager.getApi().getFollowingDiaries().map(listResult -> listResult.diaries);
            }
            //notebook的日记返回格式跟全站的不太一样，需要留意
            List<Diary> diaries = base.findDiariesOfNotebook(id);
            if (diaries.isEmpty()) {
                return LoginManager.getApi().getDiariesOfNotebook(id, page)
                        .map(listItemResult -> listItemResult.items);
            } else {
                return Observable.just(diaries);
            }
        }
        return LoginManager.getApi()
                .getTodayDiaries(id);
    }

    @Override
    public void onRefresh() {
        page = 1;
        fetch();
    }

}
