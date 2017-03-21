package com.dante.diary.login;


import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.utils.KeyboardUtils;
import com.dante.diary.R;
import com.dante.diary.base.BaseFragment;
import com.dante.diary.base.Constants;
import com.dante.diary.model.DataBase;
import com.dante.diary.model.User;
import com.dante.diary.profile.ProfileFragment;
import com.dante.diary.utils.SpUtil;
import com.dante.diary.utils.UiUtils;

import butterknife.BindView;
import rx.Subscriber;
import shem.com.materiallogin.DefaultLoginView;
import shem.com.materiallogin.DefaultRegisterView;
import shem.com.materiallogin.MaterialLoginView;

import static com.dante.diary.login.LoginManager.isLogin;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends BaseFragment {

    private static final String TAG = "LoginFragment";
    @BindView(R.id.login)
    MaterialLoginView login;

    String name;
    String psw;
    int id;

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
                UiUtils.showSnack(getView(), "请输入用户名~");
            } else if (TextUtils.isEmpty(password)) {
                UiUtils.showSnack(getView(), "请输入密码~");
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

    private void goUserProfile() {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, ProfileFragment.newInstance(id))
                .commit();

//        activity.controller.replaceFragment(ProfileFragment.newInstance(id));
    }

    private void login() {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(psw)) {
            UiUtils.showSnack(getView(), "用户名或密码不能为空哦");
            return;
        }
        KeyboardUtils.hideSoftInput(activity);

        LoginManager.login(name, psw)
                .compose(applySchedulers())
                .subscribe(new Subscriber<User>() {
                    @Override
                    public void onCompleted() {
                        goUserProfile();
                    }

                    @Override
                    public void onError(Throwable e) {
                        UiUtils.showSnack(rootView, R.string.login_failed);
                    }

                    @Override
                    public void onNext(User user) {
                        Log.d(TAG, "onNext: " + user.toString());
                        id = user.getId();
                        Log.d(TAG, "onNext: id " + id);
                        DataBase.save(realm, user);
                        saveUserAccount();
                    }
                });

    }

    private void loginSuccess() {

    }

    private void saveUserAccount() {
        SpUtil.save(Constants.NAME, name);
        SpUtil.save(Constants.PASSWORD, psw);
        SpUtil.save(Constants.ID, id);
    }

    @Override
    protected void initData() {
        if (isLogin()) {
            initUserAccount();
            goUserProfile();
        }
    }


    private void initUserAccount() {
        name = SpUtil.getString(Constants.NAME);
        psw = SpUtil.getString(Constants.PASSWORD);
        id = SpUtil.getInt(Constants.ID);
    }


}
