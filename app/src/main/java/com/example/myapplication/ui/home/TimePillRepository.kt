package com.example.myapplication.ui.home

import androidx.lifecycle.LiveData
import com.example.myapplication.data.CommentDao
import com.example.myapplication.data.DiaryDao
import com.example.myapplication.data.NotebookDao
import com.example.myapplication.data.UserDao
import com.example.myapplication.data.model.*
import com.example.myapplication.di.TimeApiClass
import com.example.myapplication.net.TimeApi
import com.example.myapplication.util.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Du Wenyu
 * 2020/12/19
 */
@Singleton
class TimePillRepository @Inject constructor(
    @TimeApiClass private val timeApi: TimeApi,
    private val diaryDao: DiaryDao,
    private val commentDao: CommentDao,
    private val userDao: UserDao,
    private val notebookDao: NotebookDao
) {

    suspend fun getLatestDiaries(page: Int, size: Int): List<Diary> {
        val result = timeApi.allTodayDiaries(page, size).diaries
        result.forEach { diary ->
            if (diary.user == null) {
                diary.user = userDao.getUserNow(diary.userId).toBaseUser()
            }
        }
        diaryDao.insertDiaries(result)
        return result
    }

    suspend fun getSpecificDiaries(type: Int = 0, id: Int, page: Int, size: Int): List<Diary> {
        val result = when (type) {
            DIARIES_TYPE_FOLLOWING -> {
                timeApi.getFollowingDiaries(page, size).diaries
            }
            DIARIES_TYPE_TOPIC -> {
                timeApi.getTopicDiaries(page, size).diaries
            }
            DIARIES_TYPE_HOME -> {
                timeApi.getTodayDiaries(id)
            }
            DIARIES_TYPE_NOTEBOOK -> {
                timeApi.getDiariesOfNotebook(id, page, size).items
            }
            else -> {
                throw NotImplementedError("getSpecificDiaries type $type not implemented")
            }
        }
        result.forEach { diary ->
            if (diary.user == null) {
                diary.user = userDao.getUserNow(diary.userId).toBaseUser()
            }
        }
        diaryDao.insertDiaries(result)
        return result
    }

    suspend fun getComments(diaryId: Int): List<Comment> {
        val result = timeApi.getDiaryComments(diaryId)
        commentDao.insertComments(result)
        return result
    }

    suspend fun postComment(diaryId: Int, data: Map<String, Any>): Comment {
        val result = timeApi.postDiaryComment(diaryId, data)
//        commentDao.insertComments(result)
        return result
    }

    suspend fun getFollowers(page: Int): UsersResult<List<User>> {
        val result = timeApi.getMyFollowers(page, CommonConfig.PAGE_SIZE_USERS)
        userDao.insertUsers(result.users)
        return result
    }

    suspend fun getFollowings(page: Int): UsersResult<List<User>> {
        val result = timeApi.getFollowings(page, CommonConfig.PAGE_SIZE_USERS)
        userDao.insertUsers(result.users)
        return result
    }

    suspend fun getNotifications(): List<TipResult> {
        val result = timeApi.getTips()
        return result
    }

    suspend fun getNotificationsHistory(): List<TipResult> {
        val result = timeApi.getTipsHistory()
        return result
    }

    suspend fun cancelFollowed(id: Int): Response<ResponseBody> {
        return timeApi.cancelFollowed(id)
    }

    suspend fun cancelFollow(id: Int): Response<ResponseBody> {
        return timeApi.unfollow(id)
    }

    suspend fun hasFollow(id: Int): Response<ResponseBody> {
        return timeApi.hasfollow(id)
    }

    suspend fun follow(id: Int): Response<ResponseBody> {
        return timeApi.follow(id)
    }


    suspend fun getProfile(id: Int): LiveData<User> {
        val result = userDao.getUser(id)
        userDao.insertUser(timeApi.getProfile(id))
        return result
    }

    suspend fun getNotebooks(userId: Int): List<Notebook> {
        val result = timeApi.getMyNotebooks(userId)
        notebookDao.insertNotebooks(result)
        return result
    }

    suspend fun createNotebook(data: Map<String, Any>): Notebook {
        val result = timeApi.createNotebook(data)
        notebookDao.insertNotebook(result)
        return result
    }

    suspend fun updateNotebook(notebookId: Int, data: Map<String, Any>): Notebook {
        val result = timeApi.updateNotebook(notebookId, data)
        return result
    }

    suspend fun createDiary(
        notebookId: Int,
        content: RequestBody?,
        topicState: RequestBody?,
        file: MultipartBody.Part?
    ): Diary {
        val result = timeApi.createDiary(notebookId, content, topicState, file)
        return result
    }

    suspend fun updateDiary(
        diaryId: Int,
        content: String,
        notebookId: Int,
    ): Diary {
        val result = timeApi.updateDiary(diaryId, content, notebookId)
        if (result.user == null) {
            result.user = userDao.getUserNow(result.userId).toBaseUser()
        }
        diaryDao.insertDiary(result)
        return result
    }

    suspend fun reportDiary(userId: Int, diaryId: Int): Response<ResponseBody> {
        return timeApi.reportDiary(userId, diaryId)
    }

    suspend fun deleteNotebook(notebookId: Int): Response<ResponseBody> {
        val result = timeApi.deleteNotebook(notebookId)
        notebookDao.deleteNotebook(notebookId)
        return result
    }

    suspend fun deleteDiary(diaryId: Int): Response<ResponseBody> {
        val result = timeApi.deleteDiary(diaryId)
        diaryDao.deleteDiary(diaryId)
        return result
    }

    suspend fun deleteComment(commentId: Int): Response<ResponseBody> {
        val result = timeApi.deleteComment(commentId)
//        commentDao.deleteComment(commentId)
        return result
    }

    suspend fun setNotebookCover(notebookId: Int, part: MultipartBody.Part) =
        timeApi.setNotebookCover(notebookId, part)

    fun getAllDiaries() = diaryDao.getDiaries()

    fun getAllNotebooks(userId: Int) = notebookDao.getNotebooks(userId)

    fun getDiaryDetails() = diaryDao.getDiaryDetails()

    fun getDiary(diaryId: Int) = diaryDao.getDiary(diaryId)

    fun getDiaryComments(diaryId: Int) = commentDao.getComments(diaryId)

}

