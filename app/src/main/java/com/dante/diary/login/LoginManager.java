package com.dante.diary.login;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import com.blankj.utilcode.utils.AppUtils;
import com.dante.diary.R;
import com.dante.diary.base.Constants;
import com.dante.diary.main.MainDiaryFragment;
import com.dante.diary.model.DataBase;
import com.dante.diary.model.Diary;
import com.dante.diary.model.User;
import com.dante.diary.net.NetService;
import com.dante.diary.net.TimeApi;
import com.dante.diary.utils.SpUtil;

import java.util.List;

import rx.Observable;

import static com.blankj.utilcode.utils.Utils.getContext;
import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by yons on 17/3/6.
 */

public class LoginManager {

    private static TimeApi api;
    private static boolean isLogin;

    public static boolean isLogin() {
        String name = SpUtil.getString(Constants.ACCOUNT);
        String psw = SpUtil.getString(Constants.PASSWORD);
        int id = SpUtil.getInt(Constants.ID);
        return !(name.isEmpty() || psw.isEmpty() || id <= 0);

    }

    public static Observable<TimeApi.DiariesResult<List<Diary>>> login() {
        return api.allTodayDiaries(1, MainDiaryFragment.FETCH_DIARY_SIZE);
    }

    public static TimeApi getApi() {
        if (api == null) {
            if (isLogin()) {
                api = NetService.getTimeApi(SpUtil.getString(Constants.ACCOUNT), SpUtil.getString(Constants.PASSWORD));
            }
        }
        return api;
    }

    public static TimeApi getRegisterApi() {
        return NetService.getRegisterApi();
    }

    public static Observable<User> login(String name, String password) {
        api = NetService.getTimeApi(name, password);
        return api.getMyProfile();
    }

    public static boolean isMe(int userId) {
        return userId > 0 && userId == SpUtil.getInt(Constants.ID);
    }

    public static int getMyId() {
        return SpUtil.getInt(Constants.ID);
    }

    public static User getMyUser() {
        User me = DataBase.getInstance().findUser(getMyId());
        Log.d(TAG, "get Account: " + me.getId());
        return me;
    }

    public static String getMyStringId() {
        return String.valueOf(SpUtil.getInt(Constants.ID));
    }

    public static void showGetLoginInfoError(Context context) {
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setMessage(R.string.cant_get_login_data)
                .setPositiveButton(R.string.go_setting, (dialog, which) -> AppUtils.getAppDetailsSettings(getContext()))
                .show();
    }
}
