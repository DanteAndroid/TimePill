package com.dante.diary.detail;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.transition.Slide;
import android.view.Gravity;

import com.dante.diary.R;
import com.dante.diary.base.BaseActivity;
import com.dante.diary.base.Constants;
import com.dante.diary.model.Diary;
import com.dante.diary.utils.SpUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.realm.Sort;

public class DiariesViewerActivity extends BaseActivity {

    private static final String TAG = "DiariesViewerActivity";
    @BindView(R.id.pager)
    ViewPager pager;
    private int position;
    private int currentPosition;
    private List<Diary> diaries;
    private DetailPagerAdapter adapter;
    private long start;
    private int notebookId;
    private boolean isTimeReversed;

    @Override
    protected void onPause() {
        SpUtil.save(Constants.VIEW_POSITION, currentPosition);
        super.onPause();
    }

    @Override
    protected int initLayoutId() {
        return R.layout.activity_diaries_viewer;
    }


    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        supportPostponeEnterTransition();
        super.initViews(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new Slide(Gravity.RIGHT));
        }

        position = getIntent().getIntExtra(Constants.POSITION, 0);
        notebookId = getIntent().getIntExtra("notebookId", 0);
        currentPosition = position;

        List<Fragment> fragments = new ArrayList<>();
        if (notebookId > 0) {
            isTimeReversed = getIntent().getBooleanExtra(Constants.TIME_REVERSE, false);
            diaries = getBase().findDiariesOfNotebook(notebookId)
                    .sort(Constants.CREATED, isTimeReversed ? Sort.DESCENDING : Sort.ASCENDING);

        } else {
            diaries = getBase().findTodayDiaries();
        }
        for (int i = 0; i < diaries.size(); i++) {
            fragments.add(DiaryDetailFragment.newInstance(diaries.get(i).getId()));
        }
        adapter = new DetailPagerAdapter(getSupportFragmentManager(), fragments);
        pager.setAdapter(adapter);
        pager.setCurrentItem(position);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private static class DetailPagerAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> fragments;

        DetailPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

}
