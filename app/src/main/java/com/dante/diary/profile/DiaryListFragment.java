package com.dante.diary.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.dante.diary.edit.EditDiaryActivity;
import com.dante.diary.login.LoginManager;
import com.dante.diary.main.DiaryListAdapter;
import com.dante.diary.main.MainDiaryFragment;
import com.dante.diary.model.Diary;
import com.dante.diary.model.Topic;
import com.dante.diary.utils.ImageProgresser;
import com.dante.diary.utils.SpUtil;
import com.dante.diary.utils.TransitionHelper;
import com.dante.diary.utils.UiUtils;
import com.google.gson.Gson;

import java.util.List;

import butterknife.BindView;
import io.realm.Sort;
import rx.Observable;
import top.wefor.circularanim.CircularAnim;

/**
 * Created by yons on 17/3/9.
 */

public class DiaryListFragment extends RecyclerFragment {
    public static final int DIARY_LIST_TYPE_USER = 0;
    public static final int DIARY_LIST_TYPE_NOTEBOOK = 1;
    public static final String FOLLOWING = "following";
    public static final String TOPIC = "topic";
    public static final String NOTEBOOK = "notebook";
    public static final String TODAY_DIARIES = "other";


    private static final String TAG = "DiaryListFragment";
    DiaryListAdapter adapter;
    @BindView(R.id.stateText)
    TextView stateText;
    @BindView(R.id.diarylistFab)
    FloatingActionButton fab;
    private int page;
    private int id;
    private String data;
    private boolean isFromNotebook;
    private boolean isTimeReversed;
    private String type;
    private Topic topic;

    public static DiaryListFragment newInstance(int id, String type, String data) {
        Bundle args = new Bundle();
        args.putInt(Constants.ID, id);
        args.putString(Constants.DATA, data);
        args.putString(Constants.TYPE, type);
        DiaryListFragment fragment = new DiaryListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int initLayoutId() {
        return R.layout.fragment_diary_list;
    }


    @Override
    protected void initViews() {
        super.initViews();
        if (getArguments() != null) {
            //有参数则获取参数id
            id = getArguments().getInt(Constants.ID);
            data = getArguments().getString(Constants.DATA);
            type = getArguments().getString(Constants.TYPE);

        } else {
            //我的日记列表
            id = SpUtil.getInt(Constants.ID);
        }
        adapter = new DiaryListAdapter(null);
        if (type.equals(NOTEBOOK)) {
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
            intent.putExtra(Constants.TIME_REVERSE, isTimeReversed);
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
        page = 1;
        if (isFromNotebook) {
            initAppBar();
            toolbar.setTitle(data);
            toolbar.setVisibility(View.VISIBLE);
            adapter.setIsFromNotebook(isFromNotebook);

        } else if (type.equals(TOPIC)) {
            topic = new Gson().fromJson(data, Topic.class);
            initAppBar();
            toolbar.setTitle("话题：" + topic.getTitle());
            toolbar.setSubtitle(topic.getIntro());
            toolbar.setVisibility(View.VISIBLE);
            fab.show();
            fab.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), EditDiaryActivity.class);
                intent.putExtra("isTopic", true);
                CircularAnim.fullActivity(getActivity(), fab)
                        .colorOrImageRes(R.color.colorAccent)
                        .duration(400)
                        .go(() -> startActivityForResult(intent, 0));
            });

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dy > 5) {
                        fab.hide();
                    } else if (dy < -10) {
                        fab.show();
                    }
                }
            });
        }


        if (!type.equals(TODAY_DIARIES)) {
            adapter.setOnLoadMoreListener(() -> fetch(), recyclerView);
        }
        toolbar.setOnClickListener(v -> {
            if (type.equals(TOPIC)) {
                int i = ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
                if (i == 0 && topic != null) {
                    UiUtils.showDetailDialog(getActivity(), topic.getIntro());
                } else {
                    scrollToTop();
                }

            } else {
                scrollToTop();
            }
        });
        fetch();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            getActivity();
            if (resultCode == Activity.RESULT_OK) {
                onRefresh();
            }
        }
    }

    protected void fetch() {
        changeRefresh(true);

        subscription = diariesSource()
                .distinct()
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
                        if (diaries.isEmpty()) {
                            adapter.loadMoreEnd();
                        } else {
                            if (page <= 1) {
                                adapter.setNewData(diaries);
                            } else {
                                adapter.addData(diaries);
                                adapter.loadMoreComplete();
                            }
                            page++;
                        }
                    }
                    changeRefresh(false);
                }, throwable -> changeRefresh(false));
    }

    private Observable<List<Diary>> diariesSource() {
        if (type.equals(FOLLOWING)) {
            return LoginManager.getApi().getFollowingDiaries(page, MainDiaryFragment.FETCH_DIARY_SIZE).map(result -> result.diaries);
        } else if (type.equals(TOPIC)) {
            return LoginManager.getApi().getTopicDiaries(page, MainDiaryFragment.FETCH_DIARY_SIZE).map(result -> result.diaries);
        } else if (type.equals(NOTEBOOK)) {
            //notebook的日记返回格式跟全站的不太一样，需要留意
            return LoginManager.getApi().getDiariesOfNotebook(id, page, MainDiaryFragment.FETCH_DIARY_SIZE)
                    .map(listItemResult -> listItemResult.items);
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
        List<Diary> diaries;
        if (isTimeReversed) {
            diaries = base.findDiariesOfNotebook(id).sort(Constants.CREATED, Sort.ASCENDING);
            isTimeReversed = false;
        } else {
            diaries = base.findDiariesOfNotebook(id).sort(Constants.CREATED, Sort.DESCENDING);
            isTimeReversed = true;
        }
        adapter.setNewData(diaries);
        recyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_order, menu);
    }


}
