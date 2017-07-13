package com.dante.diary.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.dante.diary.R;
import com.dante.diary.detail.DiaryDetailFragment;
import com.dante.diary.profile.DiaryListFragment;
import com.dante.diary.profile.ProfileFragment;

public class ViewActivity extends BaseActivity {
    public static final String TYPE_PROFILE = "profile";

    public static final String TYPE_DIARY_LIST = "diary_list";
    public static final String TYPE_DETAIL = "diary_detail";
    public static final String TYPE_TOPIC = "diary_topic";
    private static final String TAG = "ViewerActivity";
    private String type;
    private Fragment fragment;
    private int id;

    public static void viewDiaryList(Context context, int notebookId, @Nullable String notebookSubject) {
        Intent intent = new Intent(context, ViewActivity.class);
        intent.putExtra(Constants.ID, notebookId);
        intent.putExtra(Constants.TYPE, TYPE_DIARY_LIST);
        intent.putExtra(Constants.DATA, notebookSubject);
        context.startActivity(intent);
    }

    public static void viewTopicDiaries(Context context, String topic) {
        Intent intent = new Intent(context, ViewActivity.class);
        intent.putExtra(Constants.DATA, topic);
        intent.putExtra(Constants.TYPE, TYPE_TOPIC);
        context.startActivity(intent);
    }

    public static void viewProfile(Context context, int userId) {
        Intent intent = new Intent(context, ViewActivity.class);
        intent.putExtra(Constants.ID, userId);
        intent.putExtra(Constants.TYPE, TYPE_PROFILE);
        context.startActivity(intent);
    }

    public static void viewDiary(Context context, int diaryId) {
        Intent intent = new Intent(context, ViewActivity.class);
        intent.putExtra(Constants.ID, diaryId);
        intent.putExtra(Constants.TYPE, TYPE_DETAIL);
        context.startActivity(intent);
    }

    @Override
    protected int initLayoutId() {
        return R.layout.framelayout;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);

        type = getIntent().getStringExtra(Constants.TYPE);
        id = getIntent().getIntExtra(Constants.ID, 0);

        switch (type) {
            case TYPE_DETAIL:
                fragment = DiaryDetailFragment.newInstance(id);
                break;
            case TYPE_PROFILE:
                fragment = ProfileFragment.newInstance(id);
                break;
            case TYPE_DIARY_LIST:
                String subject = getIntent().getStringExtra(Constants.DATA);
                fragment = DiaryListFragment.newInstance(id, DiaryListFragment.NOTEBOOK, subject);
                break;
            case TYPE_TOPIC:
                String topic = getIntent().getStringExtra(Constants.DATA);
                fragment = DiaryListFragment.newInstance(id, DiaryListFragment.TOPIC, topic);
                break;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .commit();

    }


}
