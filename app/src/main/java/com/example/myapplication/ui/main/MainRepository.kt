package com.example.myapplication.ui.main

import com.example.myapplication.data.CommentDao
import com.example.myapplication.data.DiaryDao
import com.example.myapplication.data.model.Topic
import com.example.myapplication.di.TimeApiClass
import com.example.myapplication.net.TimeApi
import java.io.EOFException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Dante
 * 2020/12/15
 */
@Singleton
class MainRepository @Inject constructor(
    @TimeApiClass private val timeApi: TimeApi,
    private val diaryDao: DiaryDao,
    private val commentDao: CommentDao
) {

    suspend fun getTopic(): Topic? {
        try {
            return timeApi.getTopic()
        } catch (ignore: EOFException) {
            // no topic
        }
        return null
    }

}