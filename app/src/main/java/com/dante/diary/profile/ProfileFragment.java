package com.dante.diary.profile;


import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.transition.Explode;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dante.diary.R;
import com.dante.diary.base.BaseFragment;
import com.dante.diary.base.Constants;
import com.dante.diary.base.RecyclerFragment;
import com.dante.diary.base.TabPagerAdapter;
import com.dante.diary.login.LoginManager;
import com.dante.diary.model.User;
import com.dante.diary.utils.DateUtil;
import com.dante.diary.utils.UiUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import rx.Subscriber;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends BaseFragment {
    private static final String TAG = "ProfileFragment";

    @BindView(R.id.avatar)
    ImageView avatar;
    @BindView(R.id.followers)
    TextView followers;
    @BindView(R.id.following)
    TextView following;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout toolbarLayout;
    @BindView(R.id.progressBar)
    ProgressBar progress;

    @BindView(R.id.tabs)
    TabLayout tabs;
    @BindView(R.id.pager)
    ViewPager pager;
    @BindView(R.id.intro)
    TextView intro;

    @BindView(R.id.created)
    TextView created;
    @BindView(R.id.follow)
    FloatingActionButton fab;
    @BindView(R.id.followState)
    TextView followState;
    String[] titles;
    private int id;
    private User user;
    private List<RecyclerFragment> fragments = new ArrayList<>();
    private TabPagerAdapter adapter;
    private boolean hasFollow;
    private boolean isOther;

    public static ProfileFragment newInstance(int userId) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setAnimations() {
        super.setAnimations();
        id = getArguments().getInt(Constants.ID);
        if (!LoginManager.isMe(id)) {
            setEnterTransition(initTransitions());
        }
    }

    @Override
    protected boolean needNavigation() {
        return false;
    }

    @Override
    protected Transition initTransitions() {
        return new Explode();
    }

    @Override
    protected int initLayoutId() {
        return R.layout.fragment_profile;
    }

    @Override
    protected void initViews() {
        if (getArguments() != null) {
            isOther = true;
            fab.setOnClickListener(v -> follow());
            initTabs();
        }
    }

    private void follow() {
        subscription = LoginManager.getApi().follow(id)
                .compose(applySchedulers())
                .subscribe(responseBodyResponse -> {
                    changeFollowState(true);
                    try {
                        log("" + responseBodyResponse.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        compositeSubscription.add(subscription);
    }

    private void unFollow() {
        subscription = LoginManager.getApi().unfollow(id)
                .compose(applySchedulers())
                .subscribe(responseBodyResponse -> {
                    changeFollowState(false);
                    UiUtils.showSnack(rootView, getString(R.string.unfollow_success));

                    try {
                        log("" + responseBodyResponse.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        compositeSubscription.add(subscription);
    }

    private void changeFollowState(boolean hasFollow) {
        if (hasFollow) {
            fab.hide();
            followState.setText("已关注");
            followState.setVisibility(View.VISIBLE);
            followState.setOnClickListener(v -> unFollow());
        } else {
            followState.setText("关注");
            followState.setOnClickListener(v -> follow());
        }

    }

    private void loadProfile() {
        Glide.with(this)
                .load(user.getAvatarUrl())
                .bitmapTransform(new RoundedCornersTransformation(getContext(), 5, 0))
                .into(avatar);

        toolbarLayout.setTitle(user.getName());
        intro.setText(user.getIntro());
        created.setText(String.format("%s 加入胶囊",
                DateUtil.getDisplayDay(user.getCreated()))
        );

        if (!hasFollow && !LoginManager.isMe(id)) {
            fab.show();
        }

        progress.setVisibility(View.GONE);
        startPostponedEnterTransition();
    }

    private void fetch() {
        subscription = LoginManager.getApi().getProfile(id)
                .compose(applySchedulers())
                .subscribe(new Subscriber<User>() {
                    @Override
                    public void onCompleted() {
                        if (isOther) {
                            checkFollowState();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        UiUtils.showSnack(rootView, R.string.get_profile_failed);
                    }

                    @Override
                    public void onNext(User t) {
                        id = t.getId();
                        user = t;
                        base.save(t);
                    }
                });

        compositeSubscription.add(subscription);
    }

    private void checkFollowState() {
        subscription = LoginManager.getApi().hasfollow(id)
                .compose(applySchedulers())
                .subscribe(responseBodyResponse -> {
                    try {
                        String result = responseBodyResponse.body().string();
                        hasFollow = !TextUtils.isEmpty(result);
                        changeFollowState(hasFollow);
                        loadProfile();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, throwable -> {
                    fab.show();
                    changeFollowState(false);
                    loadProfile();
                    throwable.printStackTrace();
                });
        compositeSubscription.add(subscription);
    }

    private void initTabs() {
        adapter = new TabPagerAdapter(getChildFragmentManager());
        initFragments();
        pager.setAdapter(adapter);
        tabs.setTabMode(TabLayout.MODE_FIXED);
        tabs.setupWithViewPager(pager);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextView icon = (TextView) tab.getCustomView();
                icon.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView icon = (TextView) tab.getCustomView();
                icon.setTextColor(ContextCompat.getColor(getContext(), R.color.grey));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                fragments.get(tab.getPosition()).scrollToTop();
            }
        });
        setupTabsIcon();
    }

    private void setupTabsIcon() {
        TextView icon = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.tab_icon_0, (ViewGroup) rootView, false);
        icon.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_format_list_bulleted_white_24dp, 0, 0, 0);
        icon.setText(titles[0]);
        tabs.getTabAt(0).setCustomView(icon);
        TextView icon2 = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.tab_icon_1, (ViewGroup) rootView, false);
        icon2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_collections_bookmark_white_24dp, 0, 0, 0);
        icon2.setText(titles[1]);
        tabs.getTabAt(1).setCustomView(icon2);
    }

    private void initFragments() {
        titles = new String[]{getString(R.string.my_diary), getString(R.string.my_notebook)};
        fragments.add(DiaryListFragment.newInstance(id, null));
        fragments.add(NoteBookListFragment.newInstance(id));
        adapter.setFragments(fragments, titles);
    }

    @Override
    protected void initData() {
        fetch();
    }

    @Override
    public void onDestroyView() {
        fab.hide();
        super.onDestroyView();
    }
}
