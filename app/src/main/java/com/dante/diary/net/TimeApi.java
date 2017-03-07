package com.dante.diary.net;

import com.dante.diary.model.Diary;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by yons on 17/3/3.
 */

public interface TimeApi {

    /**
     * {
     * "count":1947,
     * "page":"1",
     * "page_size":"20",
     * "diaries":[
     * <p>
     * {
     * "id":11242696,
     * "user_id":100158137,
     * "notebook_id":516405,
     * "notebook_subject":"不要想太多",
     * "content":"犀利本性不用收敛 在这里放心放飞自我～",
     * "created":"2017-03-06 15:11:55",
     * "updated":"2017-03-06 15:11:55",
     * "type":2,
     * "comment_count":0,
     * "photoUrl":"http://s.timepill.net/s/w640/photos/2017-03-06/gxfnljgz.jpg",
     * "photoThumbUrl":"http://s.timepill.net/s/w240-h320/photos/2017-03-06/gxfnljgz.jpg",
     * "user":{
     * "id":100158137,
     * "name":"向隅",
     * "iconUrl":"http://s.timepill.net/user_icon/20031/s100158137.jpg?v=50"
     * }
     * },
     * {
     * "id":11242694,
     * "user_id":100210757,
     * "notebook_id":826509,
     * "notebook_subject":"阿p在吗",
     * "content":"听课听到想打人",
     * "created":"2017-03-06 15:11:52",
     * "updated":"2017-03-06 15:11:52",
     * "type":1,
     * "comment_count":0,
     * "photoUrl":null,
     * "photoThumbUrl":null,
     * "user":{
     * "id":100210757,
     * "name":"ea",
     * "iconUrl":"http://s.timepill.net/user_icon/20042/s100210757.jpg?v=2"
     * }
     * },...
     * ]
     * }
     */
    @GET("diaries/today")
    Observable<Result<List<Diary>>> getTodayDiaries(@Query("page") int page, @Query("page_size") int pageSize);

    class Result<T> {
        public int count;
        public int page;
        public int page_size;
        public T diaries;
    }

}
