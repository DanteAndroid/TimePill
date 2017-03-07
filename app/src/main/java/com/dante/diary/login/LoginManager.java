package com.dante.diary.login;

import com.dante.diary.model.Diary;
import com.dante.diary.net.NetService;
import com.dante.diary.net.TimeApi;

import java.util.List;

import rx.Observable;

/**
 * Created by yons on 17/3/6.
 */

public class LoginManager {

    private static TimeApi api;

    public static Observable<TimeApi.Result<List<Diary>>> login(String name, String password) {
        api = NetService.getTimeApi(name, password);
        return api.getTodayDiaries(1, 20);

    }

}
