package com.dante.diary.main;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.dante.diary.base.BottomBarActivity;
import com.dante.diary.base.Constants;
import com.dante.diary.base.RecyclerFragment;
import com.dante.diary.custom.Updater;
import com.dante.diary.detail.DiariesViewerActivity;
import com.dante.diary.edit.EditDiaryActivity;
import com.dante.diary.edit.EditNotebookActivity;
import com.dante.diary.login.LoginManager;
import com.dante.diary.model.Diary;
import com.dante.diary.setting.SettingActivity;
import com.dante.diary.setting.SettingFragment;
import com.dante.diary.utils.ImageProgresser;
import com.dante.diary.utils.Share;
import com.dante.diary.utils.SpUtil;
import com.dante.diary.utils.TransitionHelper;
import com.dante.diary.utils.UiUtils;
import com.dante.diary.utils.WrapContentLinearLayoutManager;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.List;

import butterknife.BindView;
import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import top.wefor.circularanim.CircularAnim;


public class MainDiaryFragment extends RecyclerFragment implements OrderedRealmCollectionChangeListener<RealmResults<Diary>> {
    private static final String TAG = "MainDiaryFragment";
    private static final String INDEX = "INDEX";
    private static final int FETCH_DIARY_COUNT = 20;
    String url;
    boolean isFetching;
    String title;
    int page = 1;
    BaseActivity context;
    DiaryListAdapter adapter;
    RealmResults<Diary> diaries;
    @BindView(R.id.fabMenu)
    FabSpeedDial fabMenu;
    @BindView(R.id.shadowView)
    View shadowView;


    private Intent intent;

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
        return R.layout.fragment_diary_main;
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
        setHasOptionsMenu(true);//填充menu（执行onCreateOptionsMenu）

        context = (BaseActivity) getActivity();
        layoutManager = new WrapContentLinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new DiaryListAdapter(null);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(context)
                .colorResId(R.color.grey)
                .size(2)
                .build());
        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                goDetailActivity(view, i);
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

        initFab();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (fabMenu != null) {
            fabMenu.closeMenu();
        }
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

    private void initFab() {
        if (SpUtil.getBoolean(SettingFragment.MY_HOME)) {
            return;
        }
        fabMenu.setVisibility(View.VISIBLE);
        fabMenu.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                shadowView.animate().alpha(1).start();
                return super.onPrepareMenu(navigationMenu);
            }

            @Override
            public void onMenuClosed() {
                shadowView.animate().alpha(0).start();
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                shadowView.animate().alpha(0).start();
                int id = menuItem.getItemId();
                View view = fabMenu.getChildAt(1);
                intent = null;
                if (id == R.id.action_create_diary) {
                    intent = new Intent(getContext(), EditDiaryActivity.class);
                } else if (id == R.id.action_create_notebook) {
                    intent = new Intent(getContext(), EditNotebookActivity.class);
                }
                CircularAnim.fullActivity(getActivity(), view)
                        .colorOrImageRes(R.color.colorAccent)
                        .duration(400)
                        .go(() -> startActivityForResult(intent, 0));
                return true;
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 30) {
                    CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) fabMenu.getLayoutParams();
                    int fab_bottomMargin = layoutParams.bottomMargin;
                    fabMenu.animate().translationY(fabMenu.getHeight() + fab_bottomMargin).setDuration(400).start();
                } else if (dy < -60) {
                    fabMenu.animate().translationY(0).setDuration(400).start();
                }
            }
        });

    }

    @SuppressWarnings("unchecked")
    private void goDetailActivity(View view, int i) {
        Intent intent = new Intent(context.getApplicationContext(), DiariesViewerActivity.class);
        intent.putExtra(Constants.POSITION, i);
        startActivity(intent);
    }

    @Override
    protected boolean hasFab() {
        return true;
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
                TransitionHelper.startViewer(barActivity, view, url);
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
        toolbar.setOnClickListener(v -> {
            scrollToTop();
        });
        if (LoginManager.getApi() == null) {
            LoginManager.showGetLoginInfoError(getContext());
            return;
        }
        diaries = base.findTodayDiaries();
        diaries.addChangeListener(this);
        adapter.setNewData(diaries);

        adapter.setOnLoadMoreListener(() -> {
            page = SpUtil.getInt(Constants.PAGE, 1);
            log("load more " + page);
            page++;
            fetch();
        }, recyclerView);
        adapter.disableLoadMoreIfNotFullPage();

        fetch();
        changeRefresh(true);
    }

    protected void fetch() {
        subscription = LoginManager.getApi()
                .allTodayDiaries(page, FETCH_DIARY_COUNT)
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

                    @Override
                    public void onStart() {
                        isFetching = true;
                    }

                    @Override
                    public void onCompleted() {
                        if (adapter.isLoading()) {
                            adapter.loadMoreComplete();
                        }
                        SpUtil.save(Constants.PAGE, page);
                        changeState(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        adapter.loadMoreFail();
                        changeState(false);
                        Log.e("test", "fetch: " + e.getMessage());
                    }

                    @Override
                    public void onNext(Diary diary) {
                        base.realm.executeTransactionAsync(realm -> {
                            realm.copyToRealmOrUpdate(diary);
                        });
                    }
                });

        BottomBarActivity activity = ((BottomBarActivity) getActivity());
        if (activity != null) {
            activity.fetchNotifications();
        }

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
    public void onChange(RealmResults<Diary> collection, OrderedCollectionChangeSet changeSet) {
        // `null`  means the async query returns the first time.
        if (changeSet == null) {
            log("no change");
            adapter.notifyDataSetChanged();
            return;
        }
        // For deletions, the adapter has to be notified in reverse order.
        OrderedCollectionChangeSet.Range[] deletions = changeSet.getDeletionRanges();
        for (int i = deletions.length - 1; i >= 0; i--) {
            OrderedCollectionChangeSet.Range range = deletions[i];
            log("no notifyItemRangeRemoved " + range.startIndex + " to " + range.length);
            adapter.notifyItemRangeRemoved(range.startIndex, range.length);
        }
        OrderedCollectionChangeSet.Range[] insertions = changeSet.getInsertionRanges();
        for (OrderedCollectionChangeSet.Range range : insertions) {
            log("no notifyItemRangeInserted " + range.startIndex + " to " + range.length);
            adapter.notifyItemRangeInserted(range.startIndex, range.length);
            if (page == 1) {
                scrollToTop(range.length);
//                recyclerView.scrollToPosition(0);
            }
        }

//        OrderedCollectionChangeSet.Range[] modifications = changeSet.getChangeRanges();
//        for (OrderedCollectionChangeSet.Range range : modifications) {
//            log("notifyItemRangeChanged " + range.startIndex + " to " + (range.startIndex + range.length));
//            adapter.notifyItemRangeChanged(range.startIndex, range.length);
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(getContext(), SettingActivity.class));
        } else if (id == R.id.action_share) {
            String text = SpUtil.get(Updater.SHARE_APP, getString(R.string.share_app_description));
            Log.d("test", "check: get " + text);

            Share.shareText(getContext(), text);
        } else if (id == 0) {

        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_setting, menu);
    }

}
