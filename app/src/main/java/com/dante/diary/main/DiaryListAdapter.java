package com.dante.diary.main;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.dante.diary.R;
import com.dante.diary.model.Diary;
import com.dante.diary.utils.DateUtil;
import com.dante.diary.utils.Imager;

import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static com.dante.diary.base.App.context;

public class DiaryListAdapter extends BaseQuickAdapter<Diary, BaseViewHolder> {


    public DiaryListAdapter(List<Diary> data) {
        super(R.layout.list_diary_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Diary item) {
        String avatar = item.getUser().getAvatarUrl();
        String picture = item.getPhotoThumbUrl();

        helper.setText(R.id.userName, item.getUser().getName())
                .setText(R.id.notebookSubject, item.getNotebookSubject())
                .setText(R.id.content, item.getContent())
                .setText(R.id.time, DateUtil.getDisplayTime(item.getCreated()));

        Log.d(TAG, "convert: avatar " + avatar);
        Glide.with(helper.itemView.getContext()).load(avatar)
                .bitmapTransform(new CropCircleTransformation(context))
                .crossFade()
                .into((ImageView) helper.getView(R.id.avatar));

        if (!TextUtils.isEmpty(picture)) {
            Log.d(TAG, "convert: attach " + picture);
            ImageView attachPicture = helper.getView(R.id.attachPicture);
            attachPicture.setVisibility(View.VISIBLE);
            Imager.load(helper.itemView.getContext(), picture, attachPicture);
        }


    }

}
