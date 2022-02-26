package com.example.myapplication.ui.home

import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import com.blankj.utilcode.util.TimeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.myapplication.R
import com.example.myapplication.data.model.Diary

/**
 * @author Du Wenyu
 * 2020/12/19
 */
class DiaryListAdapter :
    BaseQuickAdapter<Diary, BaseViewHolder>(R.layout.home_diary_list_item), LoadMoreModule {

    init {
        setDiffCallback(object : DiffUtil.ItemCallback<Diary>() {
            override fun areItemsTheSame(oldItem: Diary, newItem: Diary): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Diary, newItem: Diary): Boolean =
                oldItem.content == newItem.content

        })
    }

    override fun convert(holder: BaseViewHolder, item: Diary) {
        holder.getView<ViewGroup>(R.id.root).transitionName = "pager${item.id}"

        holder.setText(R.id.userName, item.user!!.name)
            .setText(R.id.time, TimeUtils.getFriendlyTimeSpanByNow(item.created))
            .setText(R.id.content, item.content)
            .setText(R.id.notebookSubject, "《${item.notebookSubject}》")
            .setText(
                R.id.commentCount,
                context.getString(R.string.comment_count, item.commentCount)
            )
            .setVisible(R.id.commentCount, item.commentCount > 0)

        val avatar = holder.getView<ImageView>(R.id.avatar)
        val picture = holder.getView<ImageView>(R.id.picture)

        Glide.with(context)
            .load(item.user!!.avatarUrl)
            .circleCrop()
            .into(avatar)

        picture.isVisible = !item.photoThumbUrl.isNullOrEmpty()

        Glide.with(context)
            .load(item.photoThumbUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(R.mipmap.ic_launcher)
            .into(picture)
    }


}