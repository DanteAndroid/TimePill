package com.dante.diary.main;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.dante.diary.base.ViewActivity;
import com.dante.diary.chat.ChatService;
import com.dante.diary.detail.DiariesViewerActivity;
import com.dante.diary.detail.PictureActivity;
import com.dante.diary.edit.EditDiaryActivity;
import com.dante.diary.edit.EditNotebookActivity;
import com.dante.diary.login.LoginManager;
import com.dante.diary.model.Diary;
import com.dante.diary.model.Topic;
import com.dante.diary.net.HttpErrorAction;
import com.dante.diary.setting.SettingActivity;
import com.dante.diary.setting.SettingFragment;
import com.dante.diary.timepill.TimePillActivity;
import com.dante.diary.utils.AppUtil;
import com.dante.diary.utils.ImageProgresser;
import com.dante.diary.utils.Imager;
import com.dante.diary.utils.SpUtil;
import com.dante.diary.utils.TransitionHelper;
import com.dante.diary.utils.UiUtils;
import com.dante.diary.utils.WrapContentLinearLayoutManager;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;
import top.wefor.circularanim.CircularAnim;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;


public class MainDiaryFragment extends RecyclerFragment implements OrderedRealmCollectionChangeListener<RealmResults<Diary>> {
    public static final int FETCH_DIARY_SIZE = 20;
    private static final String INDEX = "INDEX";
    boolean isFetching;
    int page = 1;
    BaseActivity context;
    DiaryListAdapter adapter;
    RealmResults<Diary> diaries;
    @BindView(R.id.fabMenu)
    FabSpeedDial fabMenu;
    @BindView(R.id.shadowView)
    View shadowView;
    @BindView(R.id.topicImage)
    ImageView topicImage;
    @BindView(R.id.topicTitle)
    TextView topicTitle;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout toolbarLayout;
    @BindView(R.id.appBar)
    AppBarLayout appBar;


    private Intent intent;
    private Topic topic;
    private boolean isHiding;
    private boolean isShowing;
    private boolean collapsed;

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
    public void onStart() {
        super.onStart();
        log(" onStart...");
        if (topic == null) {
            appBar.setExpanded(false);
        }
    }

    @Override
    protected void initViews() {
        super.initViews();
        setHasOptionsMenu(true);//填充menu（执行onCreateOptionsMenu）
        context = (BaseActivity) getActivity();
        layoutManager = new WrapContentLinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new DiaryListAdapter(null);
        recyclerView.setAdapter(adapter);
//        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(context)
//                .colorResId(R.color.grey)
//                .size(2)
//                .build());
        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                goDetailActivity(view, i);
            }

            @Override
            public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                int id = view.getId();
                if (id == R.id.avatar) {
                    Diary d = adapter.getItem(i);
                    if (d == null) return;
                    goProfile(d.getUserId());

                } else if (id == R.id.attachPicture) {
                    onPictureClicked(view, i);
                }
            }
        });
        initFab();
        initGuide();
    }

    private void initGuide() {
        if (SpUtil.getBoolean(Constants.IS_FIRST, true) && SpUtil.getBoolean(Constants.IS_NEW_USER)
                && fabMenu.getVisibility() == View.VISIBLE) {
            new MaterialTapTargetPrompt.Builder(getActivity())
                    .setTarget(fabMenu)
                    .setPrimaryText(R.string.new_user_create_notebook_hint)
                    .setSecondaryText(R.string.create_second_hint)
                    .setPromptStateChangeListener((prompt, state) -> {
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
//do nothing
                        }
                    })
                    .show();
        }
        SpUtil.save(Constants.IS_FIRST, false);
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
                if (dy > 5) {
                    hideFabMenu();
                } else if (dy < -30) {
                    showFabMenu();
                }
            }
        });

    }

    private void showFabMenu() {
        if (isShowing) {
            return;
        }
        fabMenu.animate().translationY(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isShowing = false;
            }
        }).setDuration(250).start();
        isShowing = true;
    }

    private void hideFabMenu() {
        if (isHiding) {
            return;
        }
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) fabMenu.getLayoutParams();
        int fab_bottomMargin = layoutParams.bottomMargin;
        fabMenu.animate().translationY(fabMenu.getHeight() + fab_bottomMargin).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isHiding = false;
            }
        }).setDuration(300).start();
        isHiding = true;
    }

    @SuppressWarnings("unchecked")
    private void goDetailActivity(View view, int i) {
        Intent intent = new Intent(context.getApplicationContext(), DiariesViewerActivity.class);
        intent.putExtra(Constants.POSITION, i);
        startActivity(intent);
//        ImageView avatar = (ImageView) view.findViewById(R.id.avatar);
//        int id = adapter.getItem(i).getId();
//        ViewCompat.setTransitionName(avatar, String.valueOf(id));
//        Intent intent = new Intent(getContext().getApplicationContext(), DiariesViewerActivity.class);
//        intent.putExtra(Constants.ID, id);
//        ActivityOptionsCompat options = ActivityOptionsCompat
//                .makeSceneTransitionAnimation(getActivity(), avatar, String.valueOf(id));
//        ActivityCompat.startActivity(getContext(), intent, options.toBundle());
    }

    @Override
    protected boolean hasFab() {
        return true;
    }

    private void onPictureClicked(View view, int i) {
        String url = adapter.getItem(i).getPhotoUrl();
        if (url == null) return;
        if (url.endsWith(".gif")) {
            Intent intent = new Intent(getActivity().getApplicationContext(), PictureActivity.class);
            intent.putExtra("isGif", true);
            intent.putExtra(Constants.URL, url);
            startActivity(intent);
            return;
        }

        final ProgressBar progressBar = ImageProgresser.attachProgress(view);
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
        ChatService.loginChatServer();
        toolbar.setVisibility(View.VISIBLE);
        toolbarLayout.setOnClickListener(v -> goTopic());
        toolbar.setOnClickListener(v -> {
            goTopic();
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
            page++;
            fetch();
        }, recyclerView);
        adapter.disableLoadMoreIfNotFullPage();
        fetch();
        changeRefresh(true);
    }

    private void goTopic() {
        int index = ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
        if (index == 0) {
            if (topic == null) {
                Toast.makeText(context, R.string.no_topic_today, Toast.LENGTH_SHORT).show();
            } else {
                ViewActivity.viewTopicDiaries(getActivity(), new Gson().toJson(topic));
            }
        } else {
            scrollToTop();
        }
    }

    protected void fetch() {
        fetchTopic();

        subscription = LoginManager.getApi()
                .allTodayDiaries(page, FETCH_DIARY_SIZE)
                .compose(applySchedulers())
                .map(listResult -> listResult.diaries)
                .flatMap(Observable::from)
                .buffer(3)
                .distinct()
                .subscribe(new Subscriber<List<Diary>>() {

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
                    }

                    @Override
                    public void onNext(List<Diary> diary) {
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

    private void fetchTopic() {
        subscription = LoginManager.getApi().getTopic()
                .compose(applySchedulers())
                .subscribe(response -> {
                    String result;
                    try {
                        result = response.body().string();
                        if (result.isEmpty()) {
                            SpUtil.remove(Constants.TOPIC_PICTURE);
                            recyclerView.setNestedScrollingEnabled(false);
                            new Handler().postDelayed(() -> appBar.setExpanded(false), 400);
                        } else {
                            Topic topic = new Gson().fromJson(result, Topic.class);
                            MainDiaryFragment.this.topic = topic;
                            SpUtil.save(Constants.TOPIC_PICTURE, topic.getImageUrl());
                            appBar.setExpanded(true);
                            recyclerView.setNestedScrollingEnabled(true);
                            appBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
                                collapsed = verticalOffset == 0;
                            });
                            toolbarLayout.setTitle("今日话题：" + topic.getTitle());
                            Imager.load(MainDiaryFragment.this, topic.getImageUrl(), topicImage);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, new HttpErrorAction<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        super.call(throwable);
                        if (!TextUtils.isEmpty(errorMessage)) {
                            UiUtils.showSnack(rootView, errorMessage);
                        }
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
    public void onChange(RealmResults<Diary> collection, OrderedCollectionChangeSet changeSet) {
        // `null`  means the async query returns the first time.
        if (changeSet == null) {
            adapter.notifyDataSetChanged();
            return;
        }
        // For deletions, the adapter has to be notified in reverse order.
        OrderedCollectionChangeSet.Range[] deletions = changeSet.getDeletionRanges();
        for (int i = deletions.length - 1; i >= 0; i--) {
            OrderedCollectionChangeSet.Range range = deletions[i];
            adapter.notifyItemRangeRemoved(range.startIndex, range.length);
        }

        OrderedCollectionChangeSet.Range[] insertions = changeSet.getInsertionRanges();
        for (OrderedCollectionChangeSet.Range range : insertions) {
            adapter.notifyItemRangeInserted(range.startIndex, range.length);
            if (page == 1) {
                scrollToTop(range.length);
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
        } else if (id == R.id.night_mode) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                UiUtils.showSnack(rootView, R.string.android_version_is_old);
            } else {
                UiModeManager modeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
                boolean enableNight = !(modeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES);
                modeManager.setNightMode(enableNight ? UiModeManager.MODE_NIGHT_YES : UiModeManager.MODE_NIGHT_NO);
                SpUtil.save(Constants.IS_NIGHT, enableNight);
            }
        } else if (id == R.id.time_pill) {
            startActivity(new Intent(getActivity(), TimePillActivity.class));
        } else if (id == R.id.donate) {
            AppUtil.donate(getActivity());
        } else if (id == R.id.hongbao) {
            AppUtil.openBrowser(getActivity(), Constants.ALI_PAY_HONGBAO);
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(SpUtil.getBoolean("auto_night_mode") ?
                R.menu.menu_setting_basic : R.menu.menu_setting, menu);
        MenuItem item = menu.findItem(R.id.night_mode);
        if (item != null) {
            item.setTitle(SpUtil.getBoolean(Constants.IS_NIGHT) ? R.string.day_mode : R.string.night_mode);
        }
    }

}
