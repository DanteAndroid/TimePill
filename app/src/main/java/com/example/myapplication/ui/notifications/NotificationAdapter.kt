package com.example.myapplication.ui.notifications

import androidx.recyclerview.widget.DiffUtil
import com.blankj.utilcode.util.TimeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.myapplication.R
import com.example.myapplication.data.model.TipResult

/**
 * @author Du Wenyu
 * 2021/1/8
 */
class NotificationAdapter :
    BaseQuickAdapter<TipResult, BaseViewHolder>(R.layout.notification_list_item), LoadMoreModule {

    init {
        setDiffCallback(object : DiffUtil.ItemCallback<TipResult>() {
            override fun areItemsTheSame(oldItem: TipResult, newItem: TipResult): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: TipResult, newItem: TipResult): Boolean =
                oldItem.content.commentId == newItem.content.commentId

        })
    }

    override fun convert(holder: BaseViewHolder, item: TipResult) {
        val user = if (item.isComment) item.content.commentUser else item.content.followUser
        holder.setText(R.id.userName, user?.name ?: context.getString(R.string.unknown_user))
            .setText(
                R.id.tipContent,
                if (item.isComment) R.string.tip_comment else R.string.tip_follow
            )!!.setText(R.id.createTime, TimeUtils.getFriendlyTimeSpanByNow(item.created))

//
    }


}