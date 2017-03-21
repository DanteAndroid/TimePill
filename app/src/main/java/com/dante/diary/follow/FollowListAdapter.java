package com.dante.diary.follow;

import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.dante.diary.R;
import com.dante.diary.model.User;
import com.dante.diary.utils.DateUtil;
import com.dante.diary.utils.Imager;

import java.util.List;

/**
 * Created by yons on 17/3/17.
 */

public class FollowListAdapter extends BaseQuickAdapter<User, BaseViewHolder> {


    public FollowListAdapter(List<User> data) {
        super(R.layout.list_follow_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, User item) {
        helper.setText(R.id.name, item.getName())
                .setText(R.id.created, String.format("%s 加入", DateUtil.getDisplayDay(item.getCreated())))
                .setText(R.id.intro, item.getIntro());

        ImageView avatarView = helper.getView(R.id.avatar);
        Imager.loadAvatar(helper.itemView.getContext(), item.getAvatarUrl(), avatarView);

    }
}
