package com.example.myapplication.widget

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

/**
 * @author Du Wenyu
 * 2020/12/26
 */
class AvatarDividerItemDecoration(private val drawable: Drawable) : RecyclerView.ItemDecoration() {

    private val mBounds = Rect()

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.set(0, 0, 0, drawable.intrinsicHeight)
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.layoutManager == null) return

        drawVertical(c, parent)
    }

    private fun drawVertical(canvas: Canvas, parent: RecyclerView) {
        if (parent.childCount <= 0) return
        canvas.save()

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            parent.layoutManager!!.getDecoratedBoundsWithMargins(child, mBounds)
            (child as? ViewGroup)?.getChildAt(1)?.let {
                val left = it.left
                val right = parent.width

                val bottom = (mBounds.bottom + child.getTranslationY().roundToInt())
                val top: Int = bottom - drawable.intrinsicHeight

                drawable.setBounds(left, top, right, bottom)
                drawable.draw(canvas)
            }
        }

        canvas.restore()
    }


}