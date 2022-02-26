package com.example.myapplication.ui.me

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.myapplication.R
import com.example.myapplication.data.model.Notebook
import com.example.myapplication.util.isMyId
import java.lang.String

/**
 * @author Du Wenyu
 * 2021/5/23
 */
class NotebookAdapter : BaseQuickAdapter<Notebook, BaseViewHolder>(R.layout.notebook_item) {


    override fun convert(holder: BaseViewHolder, item: Notebook) {
        val context: Context = holder.itemView.context
        holder.setVisible(R.id.isPrivate, item.isPublic.not())
        holder.setVisible(R.id.notExpired, item.isExpired.not())

        Glide.with(context)
            .load(item.coverUrl)
            .placeholder(R.drawable.default_cover)
            .into(holder.getView(R.id.cover) as ImageView)
        if (item.userId.isMyId()) {
            holder.setVisible(R.id.more, true)
        }
        holder.setText(R.id.notebookSubject, item.subject)
            .setText(
                R.id.createdToExpired,
                String.format("%s ~ %s", item.created, item.expired)
            )


        val expireState: TextView = holder.getView(R.id.expireState)
        if (item.isExpired) {
            expireState.setText(R.string.expired)
            expireState.setTextColor(
                ContextCompat.getColor(
                    context,
                    android.R.color.tertiary_text_light
                )
            )
        } else {
            expireState.setText(R.string.not_expired)
            expireState.setTextColor(
                ContextCompat.getColor(
                    context,
                    android.R.color.secondary_text_light
                )
            )
        }
    }


}