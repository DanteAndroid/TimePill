package com.dante.diary.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.dante.diary.R;
import com.dante.diary.base.Constants;
import com.dante.diary.base.RecyclerFragment;
import com.dante.diary.base.ViewActivity;
import com.dante.diary.detail.DiariesViewerActivity;
import com.dante.diary.login.LoginManager;
import com.dante.diary.main.DiaryListAdapter;
import com.dante.diary.model.Diary;
import com.dante.diary.utils.ImageProgresser;
import com.dante.diary.utils.SpUtil;
import com.dante.diary.utils.TransitionHelper;

import java.util.List;

import butterknife.BindView;
import io.realm.Sort;
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
    @BindView(R.id.stateText)
    TextView stateText;
    private List<Diary> diaries;
    private int page = 1;
    private int id;
    private String subject;
    private boolean isFromNotebook;
    private boolean isTimeReversed;

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
        if (getArguments() != null) {
            //有参数则获取参数id
            id = getArguments().getInt(Constants.ID);
            subject = getArguments().getString(Constants.DATA);
        } else {
            //我的日记列表
            id = SpUtil.getInt(Constants.ID);
        }
        adapter = new DiaryListAdapter(null);
        if (!TextUtils.isEmpty(subject) && !subject.equals(FOLLOWING)) {
            isFromNotebook = true;
            setHasOptionsMenu(true);
            adapter = new DiaryListAdapter(R.layout.list_diary_item_expired, null);
        }

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

            }

            @Override
            public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                int id = view.getId();
                if (id == R.id.avatar) {
                    goProfile(adapter.getItem(i).getUserId());
                } else if (id == R.id.attachPicture) {
                    onPictureClicked(view, i);
                }

            }
        });
    }

    private void onPictureClicked(View view, int i) {
        final ProgressBar progressBar = ImageProgresser.attachProgress(view);
        String url = adapter.getItem(i).getPhotoUrl();
        Glide.with(this).load(url).listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                progressBar.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                progressBar.setVisibility(View.GONE);
                TransitionHelper.startViewer(getActivity(), view, url);
                return false;
            }
        }).preload();
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
            ViewActivity.viewDiary(getActivity(), diaryId);
//            Fragment f = DiaryDetailFragment.newInstance(diaryId);
//            add(f);
        }
    }

    @Override
    protected void initData() {
        super.initData();
        if (isFromNotebook) {
            initAppBar();
            toolbar.setTitle(subject);
            toolbar.setVisibility(View.VISIBLE);
            adapter.setIsFromNotebook(isFromNotebook);
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
                        adapter.setNewData(null);
                        stateText.setText(R.string.no_today_diary);
                        stateText.setVisibility(View.VISIBLE);
                    } else {
                        adapter.setNewData(diaries);
                        page++;
                    }

                    changeRefresh(false);
                }, throwable -> Log.e("test", "fetch: " + throwable.getMessage()));
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_order) {
            if (isTimeReversed) {
                item.setTitle(R.string.time_order_reverse);
                reverseDiary();
            } else {
                item.setTitle(R.string.time_order_normal);
                reverseDiary();
            }
        }
        return true;
    }

    private void reverseDiary() {
        if (isTimeReversed) {
            diaries = base.findDiariesOfNotebook(id).sort(Constants.CREATED, Sort.ASCENDING);
            isTimeReversed = false;
        } else {
            diaries = base.findDiariesOfNotebook(id).sort(Constants.CREATED, Sort.DESCENDING);
            isTimeReversed = true;
        }
        adapter.setNewData(diaries);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_order, menu);
    }
}
