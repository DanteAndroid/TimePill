package com.dante.diary.profile;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.internal.NavigationMenu;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVObject;
import com.bumptech.glide.Glide;
import com.dante.diary.R;
import com.dante.diary.base.App;
import com.dante.diary.base.BaseFragment;
import com.dante.diary.base.Constants;
import com.dante.diary.base.RecyclerFragment;
import com.dante.diary.base.TabPagerAdapter;
import com.dante.diary.chat.ConversationActivity;
import com.dante.diary.edit.EditDiaryActivity;
import com.dante.diary.edit.EditNotebookActivity;
import com.dante.diary.interfaces.QueryResultCallback;
import com.dante.diary.login.LoginManager;
import com.dante.diary.model.DataBase;
import com.dante.diary.model.User;
import com.dante.diary.search.SearchActivity;
import com.dante.diary.setting.SettingFragment;
import com.dante.diary.utils.DateUtil;
import com.dante.diary.utils.SpUtil;
import com.dante.diary.utils.UiUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import rx.Subscriber;
import top.wefor.circularanim.CircularAnim;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends BaseFragment {
    private static final String TAG = "ProfileFragment";
    private static final int SHORT_INTRO_LENGTH = 50;
    @BindView(R.id.shadowView)
    View shadowView;
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
    @BindView(R.id.fabMenu)
    FabSpeedDial fabMenu;
    private int id;
    private User user;
    private List<RecyclerFragment> fragments = new ArrayList<>();
    private TabPagerAdapter adapter;
    private boolean hasFollow;
    private boolean isOther;
    private boolean meAsHome;

    public static ProfileFragment newInstance(int userId) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onPause() {
        super.onPause();
        fabMenu.closeMenu();
    }

//    @Override
//    protected void setAnimations() {
//        super.setAnimations();
//        if (!LoginManager.isMe(id)) {
//            setEnterTransition(initTransitions());
//        }
//    }

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
        setHasOptionsMenu(true);
        id = getArguments().getInt(Constants.ID);
        meAsHome = SpUtil.getBoolean(SettingFragment.MY_HOME);
        if (meAsHome && LoginManager.isMe(id)) {
            initFab();
        }
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
        progress.setVisibility(View.GONE);
        Glide.with(this)
                .load(user.getAvatarUrl())
                .bitmapTransform(new RoundedCornersTransformation(getContext(), 5, 0))
                .into(avatar);

        if (!LoginManager.isMe(id)) {
            DataBase.findTimePillUser(id, new QueryResultCallback<AVObject>() {
                @Override
                public void onExist(List<AVObject> avObjects) {
                    if (toolbar.getMenu() == null) {
                        toolbar.inflateMenu(R.menu.menu_pm);
                    } else {
                        if (toolbar.getMenu().findItem(R.menu.menu_pm) == null) {
                            toolbar.inflateMenu(R.menu.menu_pm);
                        }
                    }
                }

                @Override
                public void notExist() {

                }
            });
        }
//        boolean isIntroShort = user.getIntro() == null || user.getIntro().length() < SHORT_INTRO_LENGTH;
//        if (isIntroShort) {
//            ViewGroup.LayoutParams params = toolbarLayout.getLayoutParams();
//            params.height = (int) getResources().getDimension(R.dimen.app_bar_height);
//            toolbarLayout.setLayoutParams(params);
//        }
        intro.setText(TextUtils.isEmpty(user.getIntro()) ? "该用户简介为空" : user.getIntro());
        intro.setOnClickListener(v -> {
            UiUtils.showDetailDialog(getActivity(), user.getIntro());

        });
        created.setText(String.format("%s 加入胶囊",
                DateUtil.getDisplayDay(user.getCreated()))
        );
        toolbarLayout.setTitle(user.getName());
        if (LoginManager.isMe(id)) {
            if (toolbar.getMenu() == null) {
                toolbar.inflateMenu(R.menu.menu_search_item);
            } else {
                if (toolbar.getMenu().findItem(R.id.action_search) == null) {
                    toolbar.inflateMenu(R.menu.menu_search_item);
                }
            }

        } else if (!hasFollow) {
            fab.show();
        }
        startPostponedEnterTransition();
    }

    private void fetch() {
        subscription = LoginManager.getApi().getProfile(id)
                .compose(applySchedulers())
                .subscribe(new Subscriber<User>() {
                    @Override
                    public void onCompleted() {
                        checkFollowState();
                    }

                    @Override
                    public void onError(Throwable e) {
                        UiUtils.showSnack(rootView, R.string.get_profile_failed);
                        progress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onNext(User user) {
                        id = user.getId();
                        ProfileFragment.this.user = user;
                        base.save(user);
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
                icon.setTypeface(null, Typeface.BOLD);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView icon = (TextView) tab.getCustomView();
                icon.setTextColor(ContextCompat.getColor(getContext(), R.color.grey));
                icon.setTypeface(null, Typeface.NORMAL);
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
        fragments.add(DiaryListFragment.newInstance(id, DiaryListFragment.TODAY_DIARIES, null));
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

    private void initFab() {
        fabMenu.setVisibility(View.VISIBLE);
        fabMenu.setMenuListener(new SimpleMenuListenerAdapter() {
            Intent intent;

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
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_pm) {
            pm();
        } else if (id == R.id.action_search) {
            startActivity(new Intent(getContext(), SearchActivity.class));
        }
        return true;
    }

    private void pm() {
        if (LoginManager.isMe(id)) {
            Toast.makeText(App.context, R.string.cant_pm_to_self, Toast.LENGTH_SHORT).show();
            return;
        }
        ConversationActivity.chat(getActivity(), id);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
//        if (LoginManager.isMe(id)) {
//            inflater.inflate(R.menu.menu_search_item, menu);
//        }
    }
}
