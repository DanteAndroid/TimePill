package com.dante.diary.notification;

import android.support.v4.content.ContextCompat;
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
        TextView notification = helper.getView(R.id.notification);
        notification.setTextColor(ContextCompat.getColor(helper.itemView.getContext(),
                item.read == 1 ? R.color.tertiaryText : R.color.primaryText));

        switch (helper.getItemViewType()) {
            case TipResult.TYPE_COMMENT:
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
                sText.linkify(notification);
                notification.setText(sText);


                break;
            case TipResult.TYPE_FOLLOW:
                String followUser = item.content.getFollowUser().getName();
                String followContent = String.format(" %s 关注了你", followUser);
                SimpleText text = SimpleText.create(helper.itemView.getContext(), followContent)
                        .first(followUser)
                        .textColor(R.color.btg_global_text_blue)
                        .pressedTextColor(android.R.color.white)
                        .onClick((charSequence, range, o) -> {
                            if (listener != null) {
                                listener.onItemClick(item.content.getFollowUser().getId());
                            }
                        });
                text.linkify(notification);
                notification.setText(text);
                break;
        }
    }

    private void onNotificationClick(TextView textView) {
        textView.setTextColor(ContextCompat.getColor(mContext, R.color.tertiaryText));
    }
}
