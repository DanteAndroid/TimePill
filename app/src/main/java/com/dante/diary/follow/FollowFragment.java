package com.dante.diary.follow;

import android.os.Bundle;

import com.dante.diary.R;
import com.dante.diary.base.BaseFragment;

/**
 * Created by yons on 17/3/15.
 */

public class FollowFragment extends BaseFragment{

    public static FollowFragment newInstance() {
        Bundle args = new Bundle();

        FollowFragment fragment = new FollowFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int initLayoutId() {
        return R.layout.fragment_follow;
    }

    @Override
    protected void initViews() {

    }

    @Override
    protected void initData() {

    }
}
