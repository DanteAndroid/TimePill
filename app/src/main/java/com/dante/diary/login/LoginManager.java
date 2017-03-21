package com.dante.diary.login;

import com.dante.diary.base.Constants;
import com.dante.diary.model.Diary;
import com.dante.diary.model.User;
import com.dante.diary.net.NetService;
import com.dante.diary.net.TimeApi;
import com.dante.diary.utils.SpUtil;

import java.util.List;

import rx.Observable;

/**
 * Created by yons on 17/3/6.
 */

public class LoginManager {

    private static TimeApi api;
    private static boolean isLogin;


    public static boolean isLogin() {
        String name = SpUtil.getString(Constants.NAME);
        String psw = SpUtil.getString(Constants.PASSWORD);
        int id = SpUtil.getInt(Constants.ID);
        return !(name.isEmpty() || psw.isEmpty() || id <= 0);

    }

    public static Observable<TimeApi.DiariesResult<List<Diary>>> login() {
        return api.allTodayDiaries(1, 20);
    }

    public static TimeApi getApi() {
        if (api == null) {
            if (isLogin()) {
                api = NetService.getTimeApi(SpUtil.getString(Constants.NAME), SpUtil.getString(Constants.PASSWORD));
            }
        }
        return api;
    }
    //    public static Observable<TimeApi.Result<List<Diary>>> login(String name, String password) {
//        api = NetService.getTimeApi(name, password);
//        return api.allTodayDiaries(1, 20);
//    }

    public static Observable<User> login(String name, String password) {
        api = NetService.getTimeApi(name, password);
        return api.getMyProfile();
    }

    public static boolean isMe(int userId) {
        return userId > 0 && userId == SpUtil.getInt(Constants.ID);
    }

}
