package com.dante.diary.main;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.dante.diary.R;
import com.dante.diary.model.Diary;
import com.dante.diary.utils.DateUtil;
import com.dante.diary.utils.Imager;

import java.util.Date;
import java.util.List;


public class DiaryListAdapter extends BaseQuickAdapter<Diary, BaseViewHolder> {
    private String lastDate;
    private Date oldDate;
    private boolean isFromNotebook;

    public DiaryListAdapter(List<Diary> data) {
        super(R.layout.list_diary_item, data);
    }

    public DiaryListAdapter(int itemLayoutId, List<Diary> data) {
        super(itemLayoutId, data);
    }

    public void setIsFromNotebook(boolean isFromNotebook) {
        this.isFromNotebook = isFromNotebook;
    }

    @Override
    protected void convert(BaseViewHolder helper, Diary item) {
        helper.addOnClickListener(R.id.avatar).addOnClickListener(R.id.attachPicture);
        if (isFromNotebook) {
            convertFromNotebook(helper, item);
            return;
        }
        ImageView attach = helper.getView(R.id.attachPicture);
        TextView count = helper.getView(R.id.commentsCount);
        ImageView avatarView = helper.getView(R.id.avatar);
        TextView date = helper.getView(R.id.diaryDate);
        TextView name = helper.getView(R.id.userName);
        TextView time = helper.getView(R.id.time);
        TextView isGif = helper.getView(R.id.isGif);

        String picture = item.getPhotoThumbUrl();
        if (TextUtils.isEmpty(picture)) {
            attach.setVisibility(View.GONE);//一定要加，否则会出现图片重复
            isGif.setVisibility(View.GONE);
        } else {
            attach.setVisibility(View.VISIBLE);
            isGif.setVisibility(picture.endsWith(".gif") ? View.VISIBLE : View.GONE);
            Imager.load(helper.itemView.getContext(), picture, attach);
        }
        RelativeLayout.LayoutParams timeParams = (RelativeLayout.LayoutParams) time.getLayoutParams();
        if (item.getUser() == null) {
            //没有user对象，则是获取用户的日记列表
            name.setVisibility(View.GONE);

            String displayDay = DateUtil.getDisplayDay(item.getCreated());
            date.setText(displayDay);
            date.setVisibility(displayDay.equals(lastDate) || displayDay.equals(DateUtil.getDisplayDay(new Date())) ? View.GONE : View.VISIBLE);
            avatarView.setVisibility(View.GONE);
            lastDate = displayDay;
            timeParams.addRule(RelativeLayout.ALIGN_BASELINE, R.id.notebookSubject);
        } else {
            //其他用户，需要显示名字和头像
            date.setVisibility(View.GONE);
            avatarView.setVisibility(View.VISIBLE);
            name.setVisibility(View.VISIBLE);
            name.setText(item.getUser().getName());
            String avatar = item.getUser().getAvatarUrl();
            Imager.loadAvatar(helper.itemView.getContext(), avatar, avatarView);

            timeParams.addRule(RelativeLayout.ALIGN_BASELINE, R.id.userName);
        }
        time.setLayoutParams(timeParams);

        View content = helper.getView(R.id.content);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) content.getLayoutParams();
        if (item.getCommentCount() > 0) {
            count.setText(String.valueOf(item.getCommentCount()));
            count.setVisibility(View.VISIBLE);
            params.addRule(RelativeLayout.BELOW, R.id.commentsCount);
        } else {
            params.addRule(RelativeLayout.BELOW, R.id.notebookSubject);
            count.setVisibility(View.GONE);
        }
        content.setLayoutParams(params);

        //日记本名字、内容、时间，必填
        helper.setText(R.id.notebookSubject, String.format("《%s》",
                item.getNotebookSubject()))
                .setText(R.id.content, item.getContent())
                .setText(R.id.time, DateUtil.getTimeText(item.getCreated()));

    }

    private void convertFromNotebook(BaseViewHolder helper, Diary item) {
        if (item.getCreated() != oldDate) {
            helper.setText(R.id.dayOfDate, DateUtil.getDisplayDayOfMonth(item.getCreated()))
                    .setText(R.id.yearOfDate, DateUtil.getDisplayYear(item.getCreated()));
        }
        oldDate = item.getCreated();
        helper.setText(R.id.time, DateUtil.getTimeText(item.getCreated()))
                .setText(R.id.content, item.getContent());

        TextView count = helper.getView(R.id.commentsCount);
        if (item.getCommentCount() > 0) {
            count.setText(String.valueOf(item.getCommentCount()));
            count.setVisibility(View.VISIBLE);
        } else {
            count.setVisibility(View.GONE);
        }
    }


}
