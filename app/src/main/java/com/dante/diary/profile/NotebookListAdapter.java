package com.dante.diary.profile;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.dante.diary.R;
import com.dante.diary.login.LoginManager;
import com.dante.diary.model.Notebook;

import java.util.List;

import androidx.core.content.ContextCompat;

/**
 * Created by yons on 17/3/9.
 */

class NotebookListAdapter extends BaseQuickAdapter<Notebook, BaseViewHolder> {

    public NotebookListAdapter(List<Notebook> data) {
        super(R.layout.list_notebook_item, data);
//        setHasStableIds(true);
    }

    @Override
    protected void convert(BaseViewHolder helper, Notebook item) {
        Context context = helper.itemView.getContext();
        helper.addOnClickListener(R.id.more);
        helper.getView(R.id.isPrivate).setVisibility(item.isIsPublic() ? View.GONE : View.VISIBLE);
        helper.getView(R.id.notExpired).setVisibility(item.isExpired() ? View.GONE : View.VISIBLE);

        Glide.with(context)
                .load(item.getCoverUrl())
                .placeholder(R.drawable.default_cover)
                .into((ImageView) helper.getView(R.id.cover));
        if (LoginManager.isMe(item.getUserId())) {
            helper.getView(R.id.more).setVisibility(View.VISIBLE);
        }
        helper.setText(R.id.notebookSubject, item.getSubject())
                .setText(R.id.createdToExpired, String.format("%s ~ %s", item.getCreated(), item.getExpired()));


        TextView expireState = helper.getView(R.id.expireState);
        if (item.isExpired()) {
            expireState.setText(R.string.expired);
            expireState.setTextColor(ContextCompat.getColor(context, R.color.tertiaryText));
        } else {
            expireState.setText(R.string.notExpired);
            expireState.setTextColor(ContextCompat.getColor(context, R.color.secondText));
        }

    }
}
