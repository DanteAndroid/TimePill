package com.dante.diary.profile;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.utils.FileUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.dante.diary.R;
import com.dante.diary.base.App;
import com.dante.diary.base.Constants;
import com.dante.diary.base.RecyclerFragment;
import com.dante.diary.base.ViewActivity;
import com.dante.diary.detail.DiariesViewerActivity;
import com.dante.diary.edit.EditDiaryActivity;
import com.dante.diary.login.LoginManager;
import com.dante.diary.main.DiaryListAdapter;
import com.dante.diary.main.MainDiaryFragment;
import com.dante.diary.model.DataBase;
import com.dante.diary.model.Diary;
import com.dante.diary.model.Topic;
import com.dante.diary.net.HttpErrorAction;
import com.dante.diary.search.SearchActivity;
import com.dante.diary.utils.AppUtil;
import com.dante.diary.utils.ImageProgresser;
import com.dante.diary.utils.SpUtil;
import com.dante.diary.utils.TransitionHelper;
import com.dante.diary.utils.UiUtils;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import io.realm.Sort;
import rx.Observable;
import rx.Subscription;
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
    public static final String SEARCH = "search";
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
    private Integer notebookId = null;
    private String keyword;
    private boolean isFromMyNotebook;

    public static DiaryListFragment newInstance(int id, String type, String data) {
        Bundle args = new Bundle();
        args.putInt(Constants.ID, id);
        args.putString(Constants.DATA, data);
        args.putString(Constants.TYPE, type);
        DiaryListFragment fragment = new DiaryListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static void exportNotebook(int notebookId, View rootView) {
        DataBase base = DataBase.getInstance();
        List<Diary> diaries = base.findDiariesOfNotebook(notebookId);
        File exportFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                base.findNotebook(notebookId).getSubject() + ".txt");
        boolean success = false;
        for (int i = 0; i < diaries.size(); i++) {
            Diary diary = diaries.get(i);
            if (i == 0) {
                FileUtils.writeFileFromString(exportFile, "《" + diary.getNotebookSubject() + "》\n\n\n", false);
            }
            success = FileUtils.writeFileFromString(exportFile, diary.getCreated() + "\n\n" + diary.getContent() + "\n\n\n", true);
        }
        if (success && exportFile.length() > 0) {
            UiUtils.showSnackLong(rootView, App.context.getString(R.string.export_success)
                    , R.string.check, v -> AppUtil.openExplorer(exportFile));
        } else {
            UiUtils.showSnack(rootView, R.string.export_failed);
        }
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
        if (!type.equals(TODAY_DIARIES)) {
            adapter.setEmptyView(R.layout.empty_diary, (ViewGroup) rootView);
        }
        if (type.equals(NOTEBOOK)) {
            isFromNotebook = true;
            setHasOptionsMenu(true);
            adapter = new DiaryListAdapter(R.layout.list_diary_item_expired, null);
        }
        if (type.equals(SEARCH)) {
            keyword = data;
            if (id != 0) {
                notebookId = id;
            }
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
        Log.d(TAG, "onDiaryClicked: " + type);
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
                    log("fetch" + diaries.isEmpty());
                    if (isFromNotebook) {
                        reorderDiaries(diaries);//时间顺序
                        adapter.setEmptyView(R.layout.empty_diary);
                    }
                    if (!diaries.isEmpty()) {
                        if (isFromNotebook) {
                            base.save(diaries);
                        }
                        if (LoginManager.isMe(diaries.get(0).getUserId())) {
                            isFromMyNotebook = true;
                            getActivity().invalidateOptionsMenu();
                        }
                    }

                    if (page <= 1 && diaries.isEmpty()) {
                        adapter.setNewData(null);
                        if (type.equals(TODAY_DIARIES)) {
                            stateText.setText("没有最新日记");
                            stateText.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (diaries.isEmpty()) {
                            adapter.loadMoreEnd();
                        } else {
                            stateText.setVisibility(View.GONE);
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
                }, new HttpErrorAction<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        super.call(throwable);
                        changeRefresh(false);
                        UiUtils.showSnack(toolbar, getString(R.string.fetch_diary_error) + errorMessage);
                    }
                });
    }

    private void reorderDiaries(List<Diary> diaries) {
        Collections.sort(diaries, (o1, o2) -> {
            if (o1.getCreated() == null || o2.getCreated() == null)
                return 0;
            return o1.getCreated().compareTo(o2.getCreated());
        });
    }


    private Observable<List<Diary>> diariesSource() {
        switch (type) {
            case FOLLOWING:
                return LoginManager.getApi().getFollowingDiaries(page, MainDiaryFragment.FETCH_DIARY_SIZE).map(result -> result.diaries);
            case TOPIC:
                return LoginManager.getApi().getTopicDiaries(page, MainDiaryFragment.FETCH_DIARY_SIZE).map(result -> result.diaries);
            case NOTEBOOK:
                //notebook的日记返回格式跟全站的不太一样，需要留意
                return LoginManager.getApi().getDiariesOfNotebook(id, page, MainDiaryFragment.FETCH_DIARY_SIZE)
                        .map(listItemResult -> listItemResult.items);
            case SEARCH:
                return LoginManager.getApi().search(keyword, page, MainDiaryFragment.FETCH_DIARY_SIZE, notebookId)
                        .map(listDiariesResult -> listDiariesResult.items);
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
        } else if (id == R.id.action_search) {
            Intent search = new Intent(getContext(), SearchActivity.class);
            search.putExtra(Constants.SUBJECT, data);
            search.putExtra(Constants.ID, this.id);
            startActivity(search);
        } else if (id == R.id.export) {
            final RxPermissions permissions = new RxPermissions(getActivity());
            Subscription subscription = permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .map(granted -> {
                        if (granted) {
                            return this.id;
                        } else {
                            return 0;
                        }
                    })
                    .compose(applySchedulers())
                    .subscribe(nid -> {
                        if (nid > 0) {
                            exportNotebook(nid, rootView);
                        }
                    }, throwable -> Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show());
            compositeSubscription.add(subscription);
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
        inflater.inflate(R.menu.menu_notebook_list, menu);
        if (isFromMyNotebook) {
            menu.findItem(R.id.action_search).setVisible(true);
            menu.findItem(R.id.export).setVisible(true);
        }
    }


}
