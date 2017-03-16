package com.dante.diary.net;

import com.dante.diary.model.Comment;
import com.dante.diary.model.Diary;
import com.dante.diary.model.Notebook;
import com.dante.diary.model.User;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by yons on 17/3/3.
 */

public interface TimeApi {

    @GET("diaries/today")
    Observable<Result<List<Diary>>> allTodayDiaries(@Query("page") int page, @Query("page_size") int pageSize);

    @GET("users/{user_id}/diaries")
    Observable<List<Diary>> getTodayDiaries(@Path("user_id") int userId);

    @GET("diaries/{diary_id}")
    Observable<Diary> getDiaryDetail(@Path("diary_id") int diaryId);

    @GET("diaries/{diary_id}/comments")
    Observable<List<Comment>> getDiaryComments(@Path("diary_id") int diaryId);

    @FormUrlEncoded
    @POST("diaries/{diary_id}/comments")
    Observable<Comment> postDiaryComment(@Path("diary_id") int diaryId, @FieldMap Map<String, Object> data);

    @DELETE("comments/{id}")
    Observable<Response<ResponseBody>> deleteComment(@Path("id") int commentId);

    //获得登录用户资料
    @GET("users/my")
    Observable<User> getMyProfile();

    @GET("users/{user_id}")
    Observable<User> getProfile(@Path("user_id") int userId);

    @GET("users/{user_id}/notebooks")
    Observable<List<Notebook>> getMyNotebooks(@Path("user_id") int userId);

    @GET("notebooks/{notebook_id}/diaries")
    Observable<ItemResult<List<Diary>>> getDiariesOfNotebook(@Path("notebook_id") int notebookId, @Query("page") int page);

    class Result<T> {
        public int count;
        public int page;
        public int page_size;
        public T diaries;
    }

    class ItemResult<T> {
        public int count;
        public int page;
        public int page_size;
        public T items;
    }

}
