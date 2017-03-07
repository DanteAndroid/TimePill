package com.dante.diary.main;


import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.DividerItemDecoration;
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
import com.dante.diary.model.DataBase;
import com.dante.diary.model.Diary;
import com.dante.diary.utils.SpUtil;
import com.dante.diary.utils.UiUtils;
import com.dante.diary.utils.WrapContentLinearLayoutManager;

import java.util.List;

import io.realm.RealmResults;


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
//        adapter.divi
        adapter.isFirstOnly(false);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(context, layoutManager.getOrientation()));
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
        diaries = DataBase.findDiaries(realm, "");
        adapter.setNewData(diaries);

        adapter.setOnLoadMoreListener(() -> {
            page = SpUtil.getInt(Constants.PAGE, 1);
            page++;
            log("load more ", page);
            fetch();
        });

        if (diaries.isEmpty()) {
            adapter.setEnableLoadMore(false);
            firstFetch = true;
            fetch();
            changeState(true);
        }
    }

    private void fetch() {
        Log.d(TAG, "fetch: ");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                changeState(false);
            }
        }, 3000);
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
