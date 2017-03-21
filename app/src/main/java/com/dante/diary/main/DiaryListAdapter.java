package com.dante.diary.main;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.dante.diary.R;
import com.dante.diary.interfaces.OnItemClickListener;
import com.dante.diary.model.Diary;
import com.dante.diary.utils.DateUtil;
import com.dante.diary.utils.Imager;
import com.dante.diary.utils.TimeUtil;

import java.util.List;

public class DiaryListAdapter extends BaseQuickAdapter<Diary, BaseViewHolder> {
    private OnItemClickListener listener;

    public DiaryListAdapter(List<Diary> data) {
        super(R.layout.list_diary_item, data);
    }

    public DiaryListAdapter(int itemLayoutId, List<Diary> data) {
        super(itemLayoutId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Diary item) {
        ImageView avatarView = helper.getView(R.id.avatar);
        helper.addOnClickListener(R.id.avatar).addOnClickListener(R.id.attachPicture);

        TextView date = helper.getView(R.id.diaryDate);
        if (item.getUser() == null) {
            //没有user对象，则是获取的自己的日记
            Log.d(TAG, "convert: getUser() == null" + item.getNotebookSubject());
            date.setVisibility(View.VISIBLE);
            avatarView.setVisibility(View.GONE);
            date.setText(DateUtil.getDisplayDay(item.getCreated()));

        } else {
            //其他用户，需要显示名字和头像
            date.setVisibility(View.GONE);
            avatarView.setVisibility(View.VISIBLE);

            helper.setText(R.id.userName, item.getUser().getName());
            String avatar = item.getUser().getAvatarUrl();
            Imager.loadAvatar(helper.itemView.getContext(), avatar, avatarView);
        }

        TextView count = helper.getView(R.id.commentsCount);
        if (item.getCommentCount() > 0) {
            count.setText(String.valueOf(item.getCommentCount()));
            count.setVisibility(View.VISIBLE);

        } else {
            count.setVisibility(View.GONE);
        }

        //日记本名字、内容、时间，必填
        helper.setText(R.id.notebookSubject, String.format("《" + "%s" + "》",
                item.getNotebookSubject()))
                .setText(R.id.content, item.getContent())
                .setText(R.id.time, TimeUtil.getTimeText(item.getCreated()));


        String picture = item.getPhotoThumbUrl();
        ImageView attachPicture = helper.getView(R.id.attachPicture);
        if (!TextUtils.isEmpty(picture)) {
            attachPicture.setVisibility(View.VISIBLE);
            Imager.load(helper.itemView.getContext(), picture, attachPicture);
        } else {
            attachPicture.setVisibility(View.GONE);//一定要加，否则会出现图片重复
        }
    }


}
