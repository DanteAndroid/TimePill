package com.dante.diary.profile;


import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dante.diary.R;
import com.dante.diary.base.BaseFragment;
import com.dante.diary.base.Constants;
import com.dante.diary.base.RecyclerFragment;
import com.dante.diary.login.LoginManager;
import com.dante.diary.model.DataBase;
import com.dante.diary.model.User;
import com.dante.diary.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import rx.Subscriber;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends BaseFragment {
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
    @BindView(R.id.follow)
    FloatingActionButton follow;

    String[] titles;

    private int id;
    private User user;
    private List<RecyclerFragment> fragments = new ArrayList<>();
    private TabPagerAdapter adapter;

    public static ProfileFragment newInstance(int userId) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected boolean needNavigation() {
        return false;
    }

    @Override
    protected void setAnimations() {
        log("setAnimations empty");
    }

    @Override
    protected int initLayoutId() {
        return R.layout.fragment_profile;
    }

    @Override
    protected void initViews() {
        if (getArguments() != null) {
            id = getArguments().getInt(Constants.ID);
//            if (id == SpUtil.getInt(Constants.ID)) {
//                //是登录用户
//                user = DataBase.findUser(realm, id);
//                if (user != null) {
//                    loadProfile();
//                    return;
//                }
//            }
            //其他用户
            fetch();
        }
    }

    private void loadProfile() {
        Glide.with(this)
                .load(user.getAvatarUrl())
                .crossFade(600)
                .into(avatar);

        toolbarLayout.setTitle(user.getName());
        intro.setText(user.getIntro());

        initTabs();
    }

    private void fetch() {
        LoginManager.getApi().getProfile(id)
                .compose(applySchedulers())
                .subscribe(new Subscriber<User>() {
                    @Override
                    public void onCompleted() {
                        loadProfile();
                    }

                    @Override
                    public void onError(Throwable e) {
                        UiUtils.showSnack(rootView, R.string.get_profile_failed);
                    }

                    @Override
                    public void onNext(User t) {
                        id = t.getId();
                        user = t;
                        DataBase.save(realm, t);
                    }
                });


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
                icon.setTextColor(getContext().getColor(android.R.color.white));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView icon = (TextView) tab.getCustomView();
                icon.setTextColor(getContext().getColor(R.color.grey));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                fragments.get(tab.getPosition())
                        .getRecyclerView()
                        .smoothScrollToPosition(0);
            }
        });
        setupTabsIcon();
    }

    private void setupTabsIcon() {
        TextView icon = (TextView) LayoutInflater.from(activity).inflate(R.layout.tab_icon_diary, (ViewGroup) rootView, false);
        tabs.getTabAt(0).setCustomView(icon);
        TextView icon2 = (TextView) LayoutInflater.from(activity).inflate(R.layout.tab_icon_notebook, (ViewGroup) rootView, false);
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
        activity.hideBottomBar();
    }


    public class TabPagerAdapter extends FragmentPagerAdapter {
        private List<RecyclerFragment> fragments;
        private String[] titles;

        TabPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        void setFragments(List<RecyclerFragment> fragments, String[] titles) {
            this.fragments = fragments;
            this.titles = titles;
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

    }
}
