package com.example.myapplication.ui.detail

import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import com.blankj.utilcode.util.TimeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.myapplication.R
import com.example.myapplication.data.model.Comment

/**
 * @author Du Wenyu
 * 2021/1/8
 */
class DiaryCommentsAdapter : BaseQuickAdapter<Comment, BaseViewHolder>(R.layout.comment_list_item) {

    init {
        setDiffCallback(object : DiffUtil.ItemCallback<Comment>() {
            override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean =
                oldItem.content == newItem.content

        })
    }

    override fun convert(holder: BaseViewHolder, item: Comment) {
        holder.setText(R.id.commentName, item.user.name)
            .setText(R.id.commentContent, getFullContent(item))
            .setText(R.id.commentTime, TimeUtils.getFriendlyTimeSpanByNow(item.created.time - 1000))

        val avatar = holder.getView<ImageView>(R.id.commentAvatar)

        Glide.with(context)
            .load(item.user.avatarUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .circleCrop()
            .into(avatar)

    }

    private fun getFullContent(item: Comment): String {
        if (item.recipient != null) {
            return context.getString(R.string.reply_to_xxx, item.recipient.name) + item.content
        }
        return item.content
    }


}