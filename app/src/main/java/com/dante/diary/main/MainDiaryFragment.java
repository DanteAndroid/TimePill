package com.dante.diary.main;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.util.Log;
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
import com.dante.diary.detail.DiaryDetailFragment;
import com.dante.diary.login.LoginManager;
import com.dante.diary.model.DataBase;
import com.dante.diary.model.Diary;
import com.dante.diary.utils.ImageProgresser;
import com.dante.diary.utils.SpUtil;
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
    String url;
    boolean isFetching;
    String title;
    int page = 1;
    BaseActivity context;
    LinearLayoutManager layoutManager;
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

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    @Override
    protected int initLayoutId() {
        return R.layout.fragment_diary_main;
    }

    @Override
    protected void setAnimations() {
        setReturnTransition(new Fade(Fade.IN));
        setEnterTransition(new Fade(Fade.IN));
//        setAllowEnterTransitionOverlap(false);
//        setAllowReturnTransitionOverlap(false);
    }

    @Override
    protected void initViews() {
        super.initViews();
        context = (BaseActivity) getActivity();
        layoutManager = new WrapContentLinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new DiaryListAdapter(null);
//        adapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_RIGHT);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(context)
                .size(2)
                .margin(48)
                .build());
//        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
//        if (animator instanceof SimpleItemAnimator) {
//            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
//        }
        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                int diaryId = adapter.getItem(i).getId();
                Fragment f = DiaryDetailFragment.newInstance(diaryId);
                activity.controller.pushFragment(f);
            }

            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int i) {
                int id = view.getId();
                if (id == R.id.avatar) {
                    onAvatarClicked(view, i);
                } else if (id == R.id.attachPicture) {
                    onPictureClicked(view, i);
                }

            }
        });

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
                activity.startViewer(view, url);
                return false;
            }
        }).preload();
    }


    private void onAvatarClicked(View view, int i) {
        goProfile(adapter.getItem(i).getUserId());
    }

    @Override
    protected boolean needNavigation() {
        return false;
    }

    @Override
    protected void initData() {
        activity.showBottomBar();
        toolbar.setTitle(getString(R.string.app_name));
        toolbar.setNavigationIcon(R.mipmap.ic_launcher);

        diaries = DataBase.allDiaries(realm, "");
        diaryList = realm.copyFromRealm(diaries);

        adapter.setNewData(diaries);

        Log.d(TAG, "fetch: ");
        if (LoginManager.getApi() == null) {
            UiUtils.showSnack(getView(), "您还未登陆");
            return;
        }

        adapter.setOnLoadMoreListener(() -> {
            page = SpUtil.getInt(Constants.PAGE, 1);
            log("load more ", page);
            page++;
            fetch();
        });

        changeState(true);
        fetch();
    }

    private void fetch() {

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
                        oldSize = diaries.size();
                    }

                    @Override
                    public void onCompleted() {
                        diaries = DataBase.allDiaries(realm, "");
                        newSize = diaries.size();
                        log(" old--" + oldSize + " new--" + newSize);
                        if (newSize > oldSize) {
                            SpUtil.save(Constants.PAGE, page);
                            if (page == 1) {
                                log("", " insert " + (newSize - oldSize));
                                adapter.notifyItemRangeInserted(0, newSize - oldSize);
                                recyclerView.smoothScrollToPosition(0);

                            } else {
                                adapter.notifyItemRangeChanged(oldSize, newSize);
                            }
                            adapter.loadMoreComplete();
                        } else {
                            adapter.loadMoreEnd();

                        }
                        changeRefresh(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        changeRefresh(false);
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Diary diary) {
                        DataBase.save(realm, diary);
                    }
                });
    }

    @Override
    public void onRefresh() {
        page = 1;
        fetch();
        if (adapter.isLoading()) {
            changeRefresh(false);
            UiUtils.showSnack(rootView, R.string.is_loading);
        }
    }

    //改变是否在加载数据的状态
    public void changeState(boolean fetching) {
        isFetching = fetching;
        changeRefresh(isFetching);
    }

    @Override
    public void onStop() {
        super.onStop();
        changeState(false);
    }


}
