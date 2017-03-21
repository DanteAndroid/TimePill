package com.dante.diary.net;

import com.dante.diary.model.Comment;
import com.dante.diary.model.Diary;
import com.dante.diary.model.Notebook;
import com.dante.diary.model.TipResult;
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
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by yons on 17/3/3.
 */

public interface TimeApi {

    @GET("diaries/today")
    Observable<DiariesResult<List<Diary>>> allTodayDiaries(@Query("page") int page, @Query("page_size") int pageSize);
    @GET("users/{user_id}/diaries")
    Observable<List<Diary>> getTodayDiaries(@Path("user_id") int userId);

    @GET("diaries/follow")
    Observable<DiariesResult<List<Diary>>> getFollowingDiaries();
    @GET("relation")
    Observable<UsersResult<List<User>>> getFollowings();
    @GET("relation/reverse")
    Observable<UsersResult<List<User>>> getMyFollowers();

    @POST("relation/{user_id}")
    Observable<Response<ResponseBody>> follow(@Path("user_id")int userId);
    @GET("relation/{user_id}")
    Observable<Response<ResponseBody>> hasfollow(@Path("user_id")int userId);
    @DELETE("relation/{user_id}")
    Observable<Response<ResponseBody>> unfollow(@Path("user_id")int userId);

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
    Observable<NotebooksResult<List<Diary>>> getDiariesOfNotebook(@Path("notebook_id") int notebookId, @Query("page") int page);

    @FormUrlEncoded
    @PUT("notebooks/{id}")
    Observable<Notebook> updateNotebook(@Path("id")int notebookId, @FieldMap Map<String, Object> data);
    @FormUrlEncoded
    @POST("notebooks")
    Observable<Notebook> createNotebook(@FieldMap Map<String, Object> data);

    @GET("tip")
    Observable<List<TipResult>> getTips();
    @POST("tip/read/{ids}")
    Observable<Response<ResponseBody>> tipsRead(@Path("ids") String ids);


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
