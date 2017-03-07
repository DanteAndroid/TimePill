package com.dante.diary.login;


import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.widget.Toast;

import com.dante.diary.R;
import com.dante.diary.base.Constants;
import com.dante.diary.base.BaseFragment;
import com.dante.diary.model.DataBase;
import com.dante.diary.model.Diary;
import com.dante.diary.net.TimeApi;
import com.dante.diary.utils.SpUtil;
import com.dante.diary.utils.UiUtils;

import java.util.List;

import butterknife.BindView;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import shem.com.materiallogin.DefaultLoginView;
import shem.com.materiallogin.DefaultRegisterView;
import shem.com.materiallogin.MaterialLoginView;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends BaseFragment {

    private static final String TAG = "LoginFragment";
    @BindView(R.id.login)
    MaterialLoginView login;

    String name;
    String psw;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    protected int initLayoutId() {
        return R.layout.fragment_login;
    }

    @Override
    protected void initViews() {
        ((DefaultLoginView) login.getLoginView()).setListener((loginUser, loginPass) -> {
            Editable userName = loginUser.getEditText().getText();
            Editable password = loginPass.getEditText().getText();

            if (TextUtils.isEmpty(userName)) {
                UiUtils.showSnack(getView(), "请输入用户名 ~");
            } else if (TextUtils.isEmpty(password)) {
                UiUtils.showSnack(getView(), "请输入密码 ~");
            } else {
                name = userName.toString();
                psw = password.toString();
                login();
            }

        });

        ((DefaultRegisterView) login.getRegisterView()).setListener((registerUser, registerPass, registerPassRep) -> {
            //Handle register
        });
    }

    private void login() {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(psw)) {
            UiUtils.showSnack(getView(), "用户名或密码不能为空哦");
            return;
        }

        LoginManager.login(name, psw)
                .compose(applySchedulers())
                .map(new Func1<TimeApi.Result<List<Diary>>, List<Diary>>() {
                    @Override
                    public List<Diary> call(TimeApi.Result<List<Diary>> listResult) {
                        return listResult.diaries;
                    }
                })
                .flatMap(new Func1<List<Diary>, Observable<Diary>>() {
                    @Override
                    public Observable<Diary> call(List<Diary> diaries) {
                        return Observable.from(diaries);
                    }
                }).subscribe(new Subscriber<Diary>() {

            @Override
            public void onStart() {

            }

            @Override
            public void onCompleted() {
                Toast.makeText(activity.getApplicationContext(), "Login success!", Toast.LENGTH_SHORT).show();
                saveLoginData();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(Diary diary) {
                DataBase.save(realm, diary);
            }
        });

    }

    private void saveLoginData() {
        SpUtil.save(Constants.NAME, name);
        SpUtil.save(Constants.PASSWORD, psw);
    }

    @Override
    protected void initData() {

    }


}
