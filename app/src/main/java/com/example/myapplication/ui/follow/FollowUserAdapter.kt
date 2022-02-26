package com.example.myapplication.ui.follow

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import com.blankj.utilcode.util.TimeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.myapplication.R
import com.example.myapplication.data.model.User

/**
 * @author Du Wenyu
 * 2021/1/8
 */
class FollowUserAdapter : BaseQuickAdapter<User, BaseViewHolder>(R.layout.follow_list_item),
    LoadMoreModule {

    init {
        setDiffCallback(object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = true

        })
    }

    override fun convert(holder: BaseViewHolder, item: User) {
        holder.getView<ViewGroup>(R.id.root).transitionName =
            context.getString(R.string.item_user_transition_name) + item.id
        holder.setText(R.id.userName, item.name)
            .setText(R.id.userIntro, item.intro)
            .setText(
                R.id.createTime,
                context.getString(
                    R.string.enter_timepill,
                    TimeUtils.getFriendlyTimeSpanByNow(item.created)
                )
            )

        val avatar = holder.getView<ImageView>(R.id.userAvatar)

        Glide.with(context)
            .load(item.avatarUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .circleCrop()
            .into(avatar)
    }


}