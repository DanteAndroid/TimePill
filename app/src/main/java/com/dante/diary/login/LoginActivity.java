package com.dante.diary.login;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.avos.avoscloud.AVObject;
import com.blankj.utilcode.utils.KeyboardUtils;
import com.dante.diary.R;
import com.dante.diary.base.BaseActivity;
import com.dante.diary.base.Constants;
import com.dante.diary.custom.LockPatternUtil;
import com.dante.diary.interfaces.QueryResultCallback;
import com.dante.diary.main.MainActivity;
import com.dante.diary.model.DataBase;
import com.dante.diary.model.User;
import com.dante.diary.net.HttpErrorAction;
import com.dante.diary.setting.SettingFragment;
import com.dante.diary.utils.DateUtil;
import com.dante.diary.utils.SpUtil;
import com.dante.diary.utils.UiUtils;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import top.wefor.circularanim.CircularAnim;

public class LoginActivity extends BaseActivity implements PatternLockViewListener {
    public static final int STARTUP_DELAY = 400;
    public static final int ANIM_DURATION = 1200;
    public static final int ITEM_DELAY = 300;
    private static final String TAG = "LoginActivity";
    @BindView(R.id.account)
    TextInputEditText accountEt;
    @BindView(R.id.accountWrapper)
    TextInputLayout accountWrapper;
    @BindView(R.id.psw)
    TextInputEditText pswEt;
    @BindView(R.id.pswWrapper)
    TextInputLayout pswWrapper;
    @BindView(R.id.reveal)
    LinearLayout reveal;
    @BindView(R.id.login)
    Button login;
    @BindView(R.id.register)
    Button register;
    @BindView(R.id.container)
    LinearLayout container;
    @BindView(R.id.timePill)
    ImageView timePill;
    @BindView(R.id.slogan)
    TextView slogan;
    @BindView(R.id.titleText)
    TextView titleText;
    @BindView(R.id.progressLogin)
    ProgressBar progressLogin;
    @BindView(R.id.progressRegister)
    ProgressBar progressRegister;
    @BindView(R.id.loginLayout)
    FrameLayout loginLayout;
    @BindView(R.id.registerLayout)
    FrameLayout registerLayout;
    @BindView(R.id.name)
    TextInputEditText nameEt;
    @BindView(R.id.nameWrapper)
    TextInputLayout nameWrapper;
    @BindView(R.id.pattern_lock)
    PatternLockView patternView;
    private int LOGO_TRANSLATION_Y = -600;
    private String nickName;
    private String password;
    private int id;
    private boolean animationStarted;
    private String emailAccount;
    private boolean animationFinished;
    private boolean isLogin;
    private boolean hasPassword;


    @Override
    protected int initLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected boolean needNavigation() {
        return false;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
//        animate();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus || animationStarted) {
            return;
        }
        animate();
        super.onWindowFocusChanged(true);
    }

    private void animate() {
        animationStarted = true;
        isLogin = LoginManager.isLogin();
        hasPassword = SpUtil.getBoolean(SettingFragment.HAS_PATTERN_LOCK);
        if (isLogin && SpUtil.getBoolean(SettingFragment.SHORT_SPLASH)) {
            slogan.setVisibility(View.GONE);
            new Handler().postDelayed(() -> {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }, 400);
            return;
        }
        slogan = findViewById(R.id.slogan);
        slogan.animate()
                .translationY(isLogin ? LOGO_TRANSLATION_Y * 2 / 3 : LOGO_TRANSLATION_Y)
                .setStartDelay(STARTUP_DELAY)
                .setDuration(isLogin ? ANIM_DURATION / 2 : ANIM_DURATION).setInterpolator(
                new DecelerateInterpolator(1.2f)).start();
        if (isLogin && SpUtil.getBoolean(SettingFragment.HAS_PATTERN_LOCK)) {
            patternView.setVisibility(View.VISIBLE);
            patternView.animate().alpha(1).setStartDelay(STARTUP_DELAY + 300).start();
            patternView.addPatternLockListener(LoginActivity.this);
        }
        timePill = findViewById(R.id.timePill);
        timePill.animate()
                .translationY(isLogin ? LOGO_TRANSLATION_Y * 2 / 3 : LOGO_TRANSLATION_Y)
                .setStartDelay(STARTUP_DELAY)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (isLogin) {
                            if (!hasPassword) {
                                loginSuccess(timePill);
                            }
                        }
                    }
                })
                .setDuration(isLogin ? ANIM_DURATION / 2 : ANIM_DURATION).setInterpolator(
                new DecelerateInterpolator(1.2f)).start();

        for (int i = 0; i < container.getChildCount(); i++) {
            if (isLogin) {
                break;
            }
            View v = container.getChildAt(i);
            ViewPropertyAnimatorCompat animator;
            if (v instanceof FrameLayout) {
                animator = ViewCompat.animate(v)
                        .scaleY(1).scaleX(1)
                        .setStartDelay((ITEM_DELAY * i) + 400)
                        .setDuration(400);

            } else {
                animator = ViewCompat.animate(v)
                        .translationY(50).alpha(1)
                        .setStartDelay((ITEM_DELAY * i) + 400)
                        .setDuration(800);
            }
            if (i == container.getChildCount() - 1) {
                animator.setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(View view) {
                        init();
                    }
                });
            }
            animator.setInterpolator(new DecelerateInterpolator()).start();
        }
    }

    private void init() {
        emailAccount = SpUtil.getString(Constants.ACCOUNT);
        password = SpUtil.getString(Constants.PASSWORD);
        accountEt.setText(emailAccount);
        pswEt.setText(password);
        login.setOnClickListener(v -> {
            moveSlogan();
            CircularAnim.show(reveal).triggerView(login).duration(300).go(() -> {
                login.setEnabled(!loginInfoInvalid());
                KeyboardUtils.showSoftInput(accountEt);
                login.setOnClickListener(v1 -> login());

            });
        });
        register.setOnClickListener(v -> {
            moveSlogan();
            nameWrapper.setVisibility(View.VISIBLE);
            loginLayout.setVisibility(View.INVISIBLE);
            pswEt.setImeActionLabel(getString(R.string.nextAction), EditorInfo.IME_ACTION_NEXT);
            CircularAnim.show(reveal).triggerView(register).duration(400).go(() -> {
                register.setEnabled(!registerInfoInvalid());
                KeyboardUtils.showSoftInput(accountEt);
                register.setOnClickListener(view -> {
                    register();
                });
            });

        });
        pswEt.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == R.id.login || id == EditorInfo.IME_NULL) {
                login();
                return true;
            }
            return false;
        });
        nameEt.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == R.id.name || id == EditorInfo.IME_NULL) {
                register();
                return true;
            }
            return false;
        });


        accountEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String result = s.toString().trim();
                if (result.length() <= 0) {
                    accountWrapper.setError(getString(R.string.username_cant_empty));
                    login.setEnabled(false);
                    register.setEnabled(false);
                } else {
                    accountWrapper.setError(null);
                    login.setEnabled(true);
                }
                emailAccount = result;
            }
        });
        pswEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String result = s.toString().trim();
                if (result.length() <= 0) {
                    pswWrapper.setError(getString(R.string.password_cant_empty));
                    login.setEnabled(false);
                    register.setEnabled(false);

                } else {
                    pswWrapper.setError(null);
                    login.setEnabled(true);
                }
                password = result;
            }
        });

        nameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String result = s.toString().trim();
                if (result.length() <= 0) {
                    pswWrapper.setError(getString(R.string.nickname_too_short));
                    register.setEnabled(false);

                } else {
                    pswWrapper.setError(null);
                    register.setEnabled(true);
                }
                nickName = result;
            }
        });
    }

    private void moveSlogan() {
//        timePill.animate().scaleX(0)
//                .scaleY(0)
//                .setDuration(400)
//                .startRecord();
        slogan.animate().alpha(0)
                .setStartDelay(450)
                .setDuration(400)
                .start();
    }

    private void register() {
        if (registerInfoInvalid()) {
            UiUtils.showSnack(register, R.string.please_enter_account_info);
            return;
        }

        KeyboardUtils.hideSoftInput(this);
        CircularAnim.hide(register).triggerView(progressRegister).go(() -> animationFinished = true);
        LoginManager.getRegisterApi().register(emailAccount, nickName, password)
                .compose(applySchedulers())
                .subscribe(user -> {
                    showRegister();
                    SpUtil.save(Constants.IS_NEW_USER, true);
                    UiUtils.showSnack(register, getString(R.string.register_success));
                    login();
                    saveAccount(user);

                }, new HttpErrorAction<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        super.call(throwable);
                        showRegister();
                        if (!TextUtils.isEmpty(errorMessage)) {
                            UiUtils.showSnackLong(register, getString(R.string.register_failed) + " " + errorMessage);
                        }
                    }
                });
    }

    private boolean registerInfoInvalid() {
        return TextUtils.isEmpty(emailAccount)
                || TextUtils.isEmpty(nickName)
                || TextUtils.isEmpty(password);
    }

    private void showRegister() {
        if (animationFinished) {
            CircularAnim.show(register).triggerView(progressRegister).go();
        } else {
            new Handler().postDelayed(() -> {
                CircularAnim.show(register).triggerView(progressRegister).go();
            }, 800);
        }
        animationFinished = false;
    }

    private void showLogin() {
        //登录失败时才show login
        if (animationFinished) {
            CircularAnim.show(login).triggerView(progressRegister).go();
            animationFinished = false;

        } else {
            new Handler().postDelayed(() -> {
                CircularAnim.show(login).triggerView(progressRegister).go();
                animationFinished = false;
            }, 800);
        }
    }

    private void login() {
        if (loginInfoInvalid()) {
            UiUtils.showSnack(login, getString(R.string.name_or_psw_is_empty));
            return;
        }
        KeyboardUtils.hideSoftInput(this);
        CircularAnim.hide(login).go(() -> {
            animationFinished = true;
        });
        LoginManager.login(emailAccount, password)
                .compose(applySchedulers())
                .subscribe(user -> {
                    saveAccount(user);
                    loginAVCloud();
                    loginSuccess(login);
                }, new HttpErrorAction<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        super.call(throwable);
                        showLogin();
                        if (!TextUtils.isEmpty(errorMessage)) {
                            UiUtils.showSnackLong(login, getString(R.string.login_failed) + " " + errorMessage);
                        }
                    }
                });
    }

    private boolean loginInfoInvalid() {
        return TextUtils.isEmpty(emailAccount) || TextUtils.isEmpty(password);
    }


    private void loginSuccess(View view) {
        eraseMemory();
        CircularAnim.fullActivity(this, view)
                .colorOrImageRes(R.color.colorAccent)
                .duration(600)
                .go(() -> {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                });
    }

    private void loginAVCloud() {
        DataBase.findTimePillUser(id, new QueryResultCallback<AVObject>() {
            @Override
            public void onExist(List<AVObject> avObjects) {

            }

            @Override
            public void notExist() {
                AVObject user = new AVObject(Constants.TP_USER);
                user.put(Constants.ID, id);
                user.saveEventually();
            }
        });
    }

    private void eraseMemory() {
        String today = DateUtil.getDisplayDay(new Date());
        String lastDate = SpUtil.getString(Constants.DATE);
        if (!today.equals(lastDate)) {
            getBase().clearAllDiaries();
        }
        SpUtil.save(Constants.DATE, today);
    }

    private void saveAccount(User user) {
        getBase().save(user);
        id = user.getId();
        SpUtil.save(Constants.ACCOUNT, emailAccount);
        SpUtil.save(Constants.PASSWORD, password);
        SpUtil.save(Constants.ID, id);
//        if (SpUtil.getBoolean("alias_set")){
//            return;
//        }
//        JPushInterface.setAlias(this, String.valueOf(id), (code, alias, set) -> {
//            if (code == 0) {
//                SpUtil.save("alias_set", true);
//            }else if (code ==6002){
//                Log.e(TAG, "gotResult: set alias time out");
//            }else {
//                Log.e(TAG, "gotResult: set alias failed. Code: "+code);
//            }
//
//
//        });
    }

    @Override
    public void onStarted() {

    }

    @Override
    public void onProgress(List<PatternLockView.Dot> progressPattern) {

    }

    @Override
    public void onComplete(List<PatternLockView.Dot> pattern) {
        LockPatternUtil.checkPattern(pattern, patternView, new LockPatternUtil.OnCheckPatternResult() {
            @Override
            public void onSuccess() {
                loginSuccess(patternView);
            }

            @Override
            public void onFailed() {

            }
        });
    }

    @Override
    public void onCleared() {

    }
}
