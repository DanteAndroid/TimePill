package com.dante.diary.main;


import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.dante.diary.R;
import com.dante.diary.base.BaseActivity;
import com.dante.diary.base.Constants;
import com.dante.diary.base.RecyclerFragment;
import com.dante.diary.login.LoginManager;
import com.dante.diary.model.DataBase;
import com.dante.diary.model.Diary;
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
        return R.layout.fragment_recycler;
    }

    @Override
    protected void initViews() {
        super.initViews();

        context = (BaseActivity) getActivity();
        layoutManager = new WrapContentLinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new DiaryListAdapter(null);
        adapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
        adapter.isFirstOnly(false);
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
                onImageClicked(view, i);
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy < 30) {
                    return;
                }
                firstPosition = layoutManager.findFirstVisibleItemPosition();
                lastPosition = layoutManager.findLastVisibleItemPosition();
            }
        });
    }

    @Override
    protected void initData() {
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

        if (diaries.isEmpty()) {
            adapter.setEnableLoadMore(false);
            firstFetch = true;
            changeState(true);
            fetch();
        }
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
                                adapter.setEnableLoadMore(true);

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


    protected void onImageClicked(View view, int position) {
        // TODO: 17/3/6
    }


}
