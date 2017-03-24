package com.dante.diary.notification;

import android.widget.TextView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.dante.diary.R;
import com.dante.diary.interfaces.IOnItemClickListener;
import com.dante.diary.model.TipResult;
import com.jaychang.st.SimpleText;

import java.util.List;

/**
 * Created by yons on 17/3/17.
 */

public class NotificationListAdapter extends BaseMultiItemQuickAdapter<TipResult, BaseViewHolder> {


    private IOnItemClickListener listener;

    public NotificationListAdapter(List<TipResult> data) {
        super(data);
        addItemType(TipResult.TYPE_COMMENT, R.layout.notification_comment);
        addItemType(TipResult.TYPE_FOLLOW, R.layout.notification_follow);
    }

    public NotificationListAdapter(List<TipResult> data, IOnItemClickListener listener) {
        super(data);
        addItemType(TipResult.TYPE_COMMENT, R.layout.notification_comment);
        addItemType(TipResult.TYPE_FOLLOW, R.layout.notification_follow);
        this.listener = listener;
    }

    @Override
    protected void convert(BaseViewHolder helper, TipResult item) {
        helper.addOnClickListener(R.id.done);
        switch (helper.getItemViewType()) {
            case TipResult.TYPE_COMMENT:
                TextView comment = helper.getView(R.id.commentNotification);

                String user = item.content.getCommentUser().getName();
                String content = String.format(" %s 回复了你的日记", user);
                SimpleText sText = SimpleText.create(helper.itemView.getContext(), content)
                        .first(user)
                        .textColor(R.color.btg_global_text_blue)
                        .pressedTextColor(android.R.color.white)
                        .onClick((charSequence, range, o) -> {
                            if (listener != null) {
                                listener.onItemClick(item.content.getCommentUser().getId());
                            }
                        });
                sText.linkify(comment);
                comment.setText(sText);
                helper.itemView.setOnClickListener(v -> onNotificationClick(comment));


                break;
            case TipResult.TYPE_FOLLOW:
                TextView follow = helper.getView(R.id.followerNotification);

                String followUser = item.content.getFollowUser().getName();
                String scontent = String.format(" %s 关注了你", followUser);
                SimpleText text = SimpleText.create(helper.itemView.getContext(), scontent)
                        .first(followUser)
                        .textColor(R.color.btg_global_text_blue)
                        .pressedTextColor(android.R.color.white)
                        .onClick((charSequence, range, o) -> {
                            if (listener != null) {
                                listener.onItemClick(item.content.getFollowUser().getId());
                            }
                        });
                text.linkify(follow);
                follow.setText(text);
                helper.itemView.setOnClickListener(v -> onNotificationClick(follow));
                break;
        }
    }

    private void onNotificationClick(TextView textView) {
        textView.setTextColor(mContext.getColor(R.color.mediumGrey));
    }
}
