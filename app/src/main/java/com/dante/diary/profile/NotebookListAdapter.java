package com.dante.diary.profile;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.dante.diary.R;
import com.dante.diary.login.LoginManager;
import com.dante.diary.model.Notebook;
import com.dante.diary.utils.Imager;

import java.util.List;

/**
 * Created by yons on 17/3/9.
 */

class NotebookListAdapter extends BaseQuickAdapter<Notebook, BaseViewHolder> {

    public NotebookListAdapter(List<Notebook> data) {
        super(R.layout.list_notebook_item, data);
        setHasStableIds(true);
    }

    @Override
    protected void convert(BaseViewHolder helper, Notebook item) {
        Context context = helper.itemView.getContext();
        helper.addOnClickListener(R.id.more);
        Imager.load(context, item.getCoverUrl(), helper.getView(R.id.cover));
        if (LoginManager.isMe(item.getUserId())) {
            helper.getView(R.id.more).setVisibility(View.VISIBLE);
        }
        helper.setText(R.id.notebookSubject, item.getSubject())
                .setText(R.id.createdToExpired, String.format("%s ~ %s", item.getCreated(), item.getExpired()));

        TextView expireState = helper.getView(R.id.expireState);
        if (item.isIsExpired()) {
            expireState.setText("已过期");
            expireState.setTextColor(ContextCompat.getColor(context, R.color.mediumGrey));
        } else {
            expireState.setText("未过期");
            expireState.setTextColor(ContextCompat.getColor(context, R.color.secondText));
        }

    }
}
