package com.dante.diary.main;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.transition.Fade;
import android.view.View;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.dante.diary.R;
import com.dante.diary.base.BaseActivity;
import com.dante.diary.base.Constants;
import com.dante.diary.base.RecyclerFragment;
import com.dante.diary.detail.DiariesViewerActivity;
import com.dante.diary.login.LoginManager;
import com.dante.diary.model.DataBase;
import com.dante.diary.model.Diary;
import com.dante.diary.utils.ImageProgresser;
import com.dante.diary.utils.SpUtil;
import com.dante.diary.utils.TransitionHelper;
import com.dante.diary.utils.UiUtils;
import com.dante.diary.utils.WrapContentLinearLayoutManager;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.List;

import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;


public class MainDiaryFragment extends RecyclerFragment {
    private static final String TAG = "MainDiaryFragment";
    private static final String INDEX = "INDEX";
    private static final int SMOOTH_SCROLL_POSITION = 40;
    String url;
    boolean isFetching;
    String title;
    int page = 1;
    BaseActivity context;
    DiaryListAdapter adapter;
    RealmResults<Diary> diaries;
    List<Diary> diaryList;

    private String mParam1;
    private String mParam2;

    public MainDiaryFragment() {
        // Required empty public constructor
    }

    public static MainDiaryFragment newInstance(int index) {
        MainDiaryFragment fragment = new MainDiaryFragment();
        Bundle args = new Bundle();
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int initLayoutId() {
        return R.layout.fragment_diary_list;
    }

//    @Override
//    protected void setAnimations() {
//        setReturnTransition(initTransitions());
//        setReenterTransition(new Slide(Gravity.RIGHT));
//        setEnterTransition(new Slide(Gravity.RIGHT));
//        setExitTransition(initTransitions());
//    }

    @Override
    protected void initViews() {
        super.initViews();
        context = (BaseActivity) getActivity();
        layoutManager = new WrapContentLinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new DiaryListAdapter(null);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(context)
                .size(2)
                .margin(48)
                .build());
        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                Intent intent = new Intent(context.getApplicationContext(), DiariesViewerActivity.class);
                intent.putExtra(Constants.POSITION, i);
                startActivity(intent);
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

    @Override
    protected boolean hasFab() {
        return true;
    }

    private void onPictureClicked(View view, int i) {
        setExitTransition(new Fade());

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
                TransitionHelper.startViewer(activity, view, url);
                return false;
            }
        }).preload();
    }


    @Override
    protected boolean needNavigation() {
        return false;
    }

    @Override
    protected void initData() {
        toolbar.setVisibility(View.VISIBLE);
        toolbar.setTitle(getString(R.string.app_name));
        toolbar.setNavigationIcon(R.mipmap.ic_launcher);
        toolbar.setOnClickListener(v -> {
            if (((LinearLayoutManager) layoutManager).findLastVisibleItemPosition() > SMOOTH_SCROLL_POSITION) {
                recyclerView.scrollToPosition(0);
            } else {
                recyclerView.smoothScrollToPosition(0);
            }

        });

        diaries = DataBase.allDiaries(realm, "");
        adapter.setNewData(diaries);

        if (LoginManager.getApi() == null) {
            UiUtils.showSnack(getView(), "您还未登陆");
            return;
        }

        adapter.setEnableLoadMore(true);
        adapter.setOnLoadMoreListener(() -> {
            page = SpUtil.getInt(Constants.PAGE, 1);
            log("load more ", page);
            page++;
            fetch();
        });

        fetch();
    }

    protected void fetch() {
        subscription = LoginManager.getApi()
                .allTodayDiaries(page, 5)
                .compose(applySchedulers())
                .map(listResult -> listResult.diaries)
                .flatMap(new Func1<List<Diary>, Observable<Diary>>() {
                    @Override
                    public Observable<Diary> call(List<Diary> diaries) {
                        return Observable.from(diaries);
                    }
                })
                .distinct()
                .subscribe(new Subscriber<Diary>() {
                    int oldSize;
                    int newSize;

                    @Override
                    public void onStart() {
                        changeState(true);
                        oldSize = diaries.size();
                    }

                    @Override
                    public void onCompleted() {
                        diaries = DataBase.findTodayDiaries(realm);

                        newSize = diaries.size();
                        log(" old--" + oldSize + " new--" + newSize);
                        if (newSize > oldSize) {
                            int add = newSize - oldSize;
                            if (page == 1) {
                                log("", " insert " + add);
                                adapter.notifyItemRangeInserted(0, add);
                                if (add > 30) {
                                    recyclerView.scrollToPosition(0);
                                } else {
                                    recyclerView.smoothScrollToPosition(0);
                                }
//                                String msg = String.format(getString(R.string.x_diaries_updated), add);
//                                UiUtils.showSnack(activity.bottomBar, msg,
//                                        R.string.view, v -> recyclerView.smoothScrollToPosition(0));

                            } else {
                                adapter.notifyItemRangeChanged(oldSize, newSize);
                            }
                        } else {
                            page++;
                        }
                        adapter.loadMoreComplete();
                        SpUtil.save(Constants.PAGE, page);
                        changeState(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        changeState(false);
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Diary diary) {
                        DataBase.save(realm, diary);
                    }
                });
        compositeSubscription.add(subscription);
    }

    @Override
    public void onRefresh() {
        if (adapter.isLoading() || isFetching) {
            UiUtils.showSnack(rootView, R.string.is_loading);
            return;
        }
        page = 1;
        fetch();
    }

    public void changeState(boolean fetching) {
        isFetching = fetching;
        changeRefresh(isFetching);
    }


    @Override
    public void onDestroy() {
        DataBase.clearAllDiaries();
        super.onDestroy();
    }
}
