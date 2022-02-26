package com.example.myapplication.net

import com.example.myapplication.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * API for time pill.
 */
interface TimeApi {
    @GET("topic")
    suspend fun getTopic(): Topic

    @GET("topic/diaries")
    suspend fun getTopicDiaries(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int
    ): DiariesResult<List<Diary>>

    @GET("diaries/today")
    suspend fun allTodayDiaries(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int
    ): DiariesResult<List<Diary>>

    @GET("diaries/search")
    suspend fun search(
        @Query("keywords") keywords: String, @Query("page") pageNum: Int,
        @Query("page_size") pageSize: Int, @Query("notebook_id") notebookId: Int?
    ): NotebooksResult<List<Diary>>

    @GET("users/{user_id}/diaries")
    suspend fun getTodayDiaries(@Path("user_id") userId: Int): List<Diary>

    @GET("diaries/follow")
    suspend fun getFollowingDiaries(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int?
    ): DiariesResult<List<Diary>>

    @GET("relation")
    suspend fun getFollowings(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int
    ): UsersResult<List<User>>

    @GET("relation/reverse")
    suspend fun getMyFollowers(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int
    ): UsersResult<List<User>>

    @POST("relation/{user_id}")
    suspend fun follow(@Path("user_id") userId: Int): Response<ResponseBody>

    @FormUrlEncoded
    @POST("reports")
    suspend fun reportDiary(
        @Field("user_id") userId: Int,
        @Field("diary_id") diaryId: Int
    ): Response<ResponseBody>

    @GET("relation/{user_id}")
    suspend fun hasfollow(@Path("user_id") userId: Int): Response<ResponseBody>

    @DELETE("relation/{user_id}")
    suspend fun unfollow(@Path("user_id") userId: Int): Response<ResponseBody>

    @GET("diaries/{diary_id}")
    suspend fun getDiaryDetail(@Path("diary_id") diaryId: Int): Diary

    @GET("diaries/{diary_id}/comments")
    suspend fun getDiaryComments(@Path("diary_id") diaryId: Int): List<Comment>

    @FormUrlEncoded
    @JvmSuppressWildcards
    @POST("diaries/{diary_id}/comments")
    suspend fun postDiaryComment(
        @Path("diary_id") diaryId: Int,
        @FieldMap data: Map<String, Any>
    ): Comment

    @DELETE("comments/{id}")
    suspend fun deleteComment(@Path("id") commentId: Int): Response<ResponseBody>

    @GET("users/my")
    suspend fun getMyProfile(): User

    @GET("users/{user_id}")
    suspend fun getProfile(@Path("user_id") userId: Int): User

    @GET("users/{user_id}/notebooks")
    suspend fun getMyNotebooks(@Path("user_id") userId: Int): List<Notebook>

    @GET("notebooks/{notebook_id}/diaries")
    suspend fun getDiariesOfNotebook(
        @Path("notebook_id") notebookId: Int,
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int
    ): NotebooksResult<List<Diary>>

    @FormUrlEncoded
    @JvmSuppressWildcards
    @PUT("notebooks/{id}")
    suspend fun updateNotebook(
        @Path("id") notebookId: Int,
        @FieldMap data: Map<String, Any>
    ): Notebook

    @FormUrlEncoded
    @JvmSuppressWildcards
    @POST("notebooks")
    suspend fun createNotebook(@FieldMap data: Map<String, Any>): Notebook

    @DELETE("notebooks/{id}")
    suspend fun deleteNotebook(@Path("id") notebookId: Int): Response<ResponseBody>

    @Multipart
    @POST("notebooks/{id}/cover")
    suspend fun setNotebookCover(
        @Path("id") notebookId: Int,
        @Part file: MultipartBody.Part
    ): Notebook

    @Multipart
    @POST("notebooks/{book_id}/diaries")
    suspend fun createDiary(
        @Path("book_id") notebookId: Int,
        @Part("content") content: RequestBody?,
        @Part("join_topic") topicState: RequestBody?,
        @Part file: MultipartBody.Part?
    ): Diary

    @DELETE("diaries/{id}")
    suspend fun deleteDiary(@Path("id") diaryId: Int): Response<ResponseBody>

    @FormUrlEncoded
    @PUT("diaries/{id}")
    suspend fun updateDiary(
        @Path("id") diaryId: Int,
        @Field("content") content: String,
        @Field("notebook_id") notebookId: Int
    ): Diary

    @GET("tip")
    suspend fun getTips(): List<TipResult>

    @GET("tip/history")
    suspend fun getTipsHistory(): List<TipResult>

    @POST("tip/read/{ids}")
    suspend fun tipsRead(@Path("ids") ids: String): Response<ResponseBody>

    @FormUrlEncoded
    @POST("users")
    suspend fun register(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("name") nickName: String
    ): Response<User>

    @FormUrlEncoded
    @PUT("users")
    suspend fun updateUserInfo(@Field("name") name: String, @Field("intro") intro: String): User

    @Multipart
    @POST("users/icon")
    suspend fun setUserIcon(@Part file: Part?): User

    @DELETE("relation/reverse/{id}")
    suspend fun cancelFollowed(@Path("id") id: Int): Response<ResponseBody>

}