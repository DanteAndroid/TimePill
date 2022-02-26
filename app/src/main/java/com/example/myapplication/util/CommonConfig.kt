package com.example.myapplication.util

import com.example.myapplication.R

/**
 * @author Du Wenyu
 * 2020/12/19
 */
object CommonConfig {

    private val homeIds: Set<Int> =
        setOf(
            R.id.navigation_home,
            R.id.navigation_follow,
            R.id.navigation_me
        )

    const val PAGE_SIZE_DIARIES = 30

    const val PAGE_SIZE_USERS = 30

    const val PRELOAD_COUNT = 5

    const val DATE_FORMAT_DAY = "yyyy-MM-dd"

    const val DIARY_TEXT_LIMIT = 5


}