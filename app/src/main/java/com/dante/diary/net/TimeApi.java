package com.dante.diary.net;

import com.dante.diary.model.Comment;
import com.dante.diary.model.Diary;
import com.dante.diary.model.Notebook;
import com.dante.diary.model.TipResult;
import com.dante.diary.model.User;

import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * API for time pill.
 */

public interface TimeApi {

    @GET("topic")
    Observable<Response<ResponseBody>> getTopic();

    @GET("topic/diaries")
    Observable<DiariesResult<List<Diary>>> getTopicDiaries(@Query("page") int page, @Query("page_size") int pageSize);

    @GET("diaries/today")
    Observable<DiariesResult<List<Diary>>> allTodayDiaries(@Query("page") int page, @Query("page_size") int pageSize);

    @GET("diaries/search")
    Observable<NotebooksResult<List<Diary>>> search(@Query("keywords") String keywords, @Query("page") int pageNum,
                                                    @Query("page_size") int pageSize, @Query("notebook_id") Integer notebookId);

    @GET("users/{user_id}/diaries")
    Observable<List<Diary>> getTodayDiaries(@Path("user_id") int userId);

    @GET("diaries/follow")
    Observable<DiariesResult<List<Diary>>> getFollowingDiaries(@Query("page") int page, @Query("page_size") Integer pageSize);

    @GET("relation")
    Observable<UsersResult<List<User>>> getFollowings(@Query("page") int page, @Query("page_size") int pageSize);

    @GET("relation/reverse")
    Observable<UsersResult<List<User>>> getMyFollowers(@Query("page") int page, @Query("page_size") int pageSize);

    @POST("relation/{user_id}")
    Observable<Response<ResponseBody>> follow(@Path("user_id") int userId);

    @FormUrlEncoded
    @POST("reports")
    Observable<Response<ResponseBody>> reportDiary(@Field("user_id") int userId, @Field("diary_id") int diaryId);

    @GET("relation/{user_id}")
    Observable<Response<ResponseBody>> hasfollow(@Path("user_id") int userId);

    @DELETE("relation/{user_id}")
    Observable<Response<ResponseBody>> unfollow(@Path("user_id") int userId);

    @GET("diaries/{diary_id}")
    Observable<Diary> getDiaryDetail(@Path("diary_id") int diaryId);

    @GET("diaries/{diary_id}/comments")
    Observable<List<Comment>> getDiaryComments(@Path("diary_id") int diaryId);

    @FormUrlEncoded
    @POST("diaries/{diary_id}/comments")
    Observable<Comment> postDiaryComment(@Path("diary_id") int diaryId, @FieldMap Map<String, Object> data);

    @DELETE("comments/{id}")
    Observable<Response<ResponseBody>> deleteComment(@Path("id") int commentId);

    @GET("users/my")
    Observable<User> getMyProfile();

    @GET("users/{user_id}")
    Observable<User> getProfile(@Path("user_id") int userId);

    @GET("users/{user_id}/notebooks")
    Observable<List<Notebook>> getMyNotebooks(@Path("user_id") int userId);

    @GET("notebooks/{notebook_id}/diaries")
    Observable<NotebooksResult<List<Diary>>> getDiariesOfNotebook(@Path("notebook_id") int notebookId, @Query("page") int page, @Query("page_size") int pageSize);

    @FormUrlEncoded
    @PUT("notebooks/{id}")
    Observable<Notebook> updateNotebook(@Path("id") int notebookId, @FieldMap Map<String, Object> data);

    @FormUrlEncoded
    @POST("notebooks")
    Observable<Notebook> createNotebook(@FieldMap Map<String, Object> data);

    @DELETE("notebooks/{id}")
    Observable<Response<ResponseBody>> deleteNotebook(@Path("id") int notebookId);

    @Multipart
    @POST("notebooks/{id}/cover")
    Observable<Notebook> setNotebookCover(@Path("id") int notebookId, @Part MultipartBody.Part file);

    @Multipart
    @POST("notebooks/{book_id}/diaries")
    Observable<Diary> createDiary(@Path("book_id") int notebookId, @Part("content") RequestBody content, @Part("join_topic") RequestBody topicState,
                                  @Nullable @Part MultipartBody.Part file);


    @DELETE("diaries/{id}")
    Observable<Response<ResponseBody>> deleteDiary(@Path("id") int diaryId);

    @FormUrlEncoded
    @PUT("diaries/{id}")
    Observable<Diary> updateDiary(@Path("id") int diaryId, @Field("content") String content, @Field("notebook_id") int notebookId);

    @GET("tip")
    Observable<List<TipResult>> getTips();

    @GET("tip/history")
    Observable<List<TipResult>> getTipsHistory();

    @POST("tip/read/{ids}")
    Observable<Response<ResponseBody>> tipsRead(@Path("ids") String ids);

    @FormUrlEncoded
    @POST("users")
    Observable<User> register(@Field("email") String email, @Field("name") String nickName, @Field("password") String password);

    @FormUrlEncoded
    @PUT("users")
    Observable<User> updateUserInfo(@Nullable @Field("name") String name, @Field("intro") String intro);

    @Multipart
    @POST("users/icon")
    Observable<User> setUserIcon(@Part MultipartBody.Part file);

    @DELETE("relation/reverse/{id}")
    Observable<Response<ResponseBody>> cancelFollowed(@Path("id") int id);

    class DiariesResult<T> {
        public int count;
        public int page;
        public int page_size;
        public T diaries;
    }

    class NotebooksResult<T> {
        public int count;
        public int page;
        public int page_size;
        public T items;
    }

    class UsersResult<T> {
        public int count;
        public T users;

    }


}
