package com.example.myapplication.util

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.ui.main.MainActivity
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * @author Du Wenyu
 * 2020/12/20
 */
val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
    throwable.printStackTrace()
}

fun CoroutineScope.safeLaunch(
    exceptionHandler: CoroutineExceptionHandler = coroutineExceptionHandler,
    launchBody: suspend () -> Unit
): Job {
    return this.launch(exceptionHandler) {
        launchBody.invoke()
    }
}

fun BottomNavigationView.hide() {
    isVisible = false
}

fun BottomNavigationView.show() {
    isVisible = true
}

fun CollapsingToolbarLayout.setCollapsible(collapsible: Boolean) {
    val params = layoutParams
    if (collapsible) {
        params.height = context.resources.getDimension(R.dimen.app_bar_height).toInt()
    } else {
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
    }
    // 可伸缩时才显示内容
    getChildAt(0)?.isVisible = collapsible
}

fun Fragment.getBottomAppBar(): BottomAppBar? = (activity as? MainActivity)?.getBottomAppBar()
fun Fragment.getFab(): FloatingActionButton? = (activity as? MainActivity)?.getFab()

fun Int.isMyId() = this == DataStoreUtil.getMyId()
fun Int.isNotMyId() = this != DataStoreUtil.getMyId()

fun NestedScrollView.scrollToBottom() {
    post {
        this.fullScroll(View.FOCUS_DOWN)
    }
}