package com.dante.diary.setting;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.blankj.utilcode.utils.AppUtils;
import com.blankj.utilcode.utils.CleanUtils;
import com.blankj.utilcode.utils.FileUtils;
import com.bugtags.library.Bugtags;
import com.dante.diary.BuildConfig;
import com.dante.diary.R;
import com.dante.diary.base.AboutActivity;
import com.dante.diary.base.App;
import com.dante.diary.base.BaseActivity;
import com.dante.diary.base.EventMessage;
import com.dante.diary.custom.BottomDialogFragment;
import com.dante.diary.custom.LockPatternUtil;
import com.dante.diary.login.LoginManager;
import com.dante.diary.utils.AppUtil;
import com.dante.diary.utils.SpUtil;
import com.dante.diary.utils.UiUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * the view in setting activity.
 */
public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    public static final String LOG_OFF = "log_off";
    public static final String FEED_BACK = "feedback";
    public static final String SECRET_MODE = "secret_mode";
    public static final String THEME_COLOR = "theme_color";
    public static final String MY_HOME = "my_home";
    public static final String PROFILE = "profile";
    public static final String ABOUT = "about";
    public static final String SHORT_SPLASH = "short_splash";
    public static final String HAS_PATTERN_LOCK = "pattern_lock";
    public static final String PATTERN_LOCK_PSW = "pattern_lock_psw";
    public static final String AUTO_NIGHT_MODE = "auto_night_mode";
    public static final String CACHE_STRATEGY = "cache_strategy";
    private static final long DURATION = 300;
    private Preference clearCache;
    private Preference feedback;
    private SwitchPreference night;
    private CheckBoxPreference my;
    private SwitchPreference patternLock;
    private ListPreference cacheStrategy;
    private Preference about;

    private View rootView;
    private long startTime;
    private boolean first = true;
    private int secretIndex;
    private Preference theme;
    private CheckBoxPreference shortSplash;
    private BottomDialogFragment patternDialog;
    private boolean hasPassword = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        clearCache = findPreference(LOG_OFF);
        feedback = findPreference(FEED_BACK);
        night = (SwitchPreference) findPreference(AUTO_NIGHT_MODE);
        my = (CheckBoxPreference) findPreference(MY_HOME);
        shortSplash = (CheckBoxPreference) findPreference(SHORT_SPLASH);
        about = findPreference(ABOUT);


        night.setOnPreferenceChangeListener((preference, newValue) -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                UiUtils.showSnack(rootView, R.string.android_version_is_old);
                return false;
            }
            if ((boolean) newValue) {
                AppUtil.autoNightMode();
            }
            EventBus.getDefault().post(new EventMessage("invalidateOptionsMenu"));
            return true;
        });
        patternLock = (SwitchPreference) findPreference(HAS_PATTERN_LOCK);
        patternLock.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean enable = (boolean) newValue;
            boolean hadLock = SpUtil.getBoolean(HAS_PATTERN_LOCK);
            if (hadLock && !enable) {
                showPatternLockDialog(getString(R.string.check_old_pattern));
                return true;
            }
            if (enable) {
                showPatternLockDialog(null);
            }
            return true;
        });
//        theme = findPreference(THEME_COLOR);
        findPreference(PROFILE).setOnPreferenceClickListener(preference -> {
            replace(new ProfilePreferenceFragment());
            return true;
        });
        findPreference("notifications").setOnPreferenceClickListener(preference -> {
            replace(new NotificationPreferenceFragment());
            return true;
        });
        refreshCache();
        about.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(getActivity().getApplicationContext(), AboutActivity.class));
            return true;
        });


        clearCache.setOnPreferenceClickListener(this);
        feedback.setOnPreferenceClickListener(this);
        my.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((boolean) newValue) {
                UiUtils.showSnack(getView(), getString(R.string.save_my_home_hint));
            }
            return true;
        });

//        theme.setOnPreferenceClickListener(preference -> {
//            Log.i("test", secretIndex + ">>>>");
//            secretStepTwo();
////                ColorPickerDialog dialog = new ColorPickerDialog(getActivity());
////                dialog.setOnColorSelectedListener(new ColorPickerDialog.OnColorSelectedListener() {
////                    @Override
////                    public void onColorSelected(Colorful.ThemeColor color) {
////                        Colorful.config(getActivity())
////                                .primaryColor(color)
////                                .apply();
////                    }
////                });
////                dialog.show();
//            return true;
//        });
    }

    private void showPatternLockDialog(String title) {
        boolean removePassword = title != null;
        patternDialog = BottomDialogFragment.create(R.layout.pattern_lock)
                .with((AppCompatActivity) getActivity())
                .gravity(Gravity.CENTER)
                .isComment(false)
                .listenDismiss(dialog -> {
                    patternLock.setChecked(hasPassword);
                })
                .bindView(v -> {
                    PatternLockView patternLockView = v.findViewById(R.id.pattern_lock);
                    TextView textView = v.findViewById(R.id.title);
                    if (removePassword) {
                        textView.setText(title);
                    }
                    patternLockView.addPatternLockListener(new PatternLockViewListener() {
                        @Override
                        public void onStarted() {

                        }

                        @Override
                        public void onProgress(List<PatternLockView.Dot> progressPattern) {

                        }

                        @Override
                        public void onComplete(List<PatternLockView.Dot> pattern) {
                            if (removePassword) {
                                LockPatternUtil.checkPattern(pattern, patternLockView, new LockPatternUtil.OnCheckPatternResult() {
                                    @Override
                                    public void onSuccess() {
                                        hasPassword = false;
                                        patternDialog.dismiss();
                                        shortSplash.setEnabled(true);
                                    }

                                    @Override
                                    public void onFailed() {
                                        patternLock.setChecked(true);
                                    }
                                });

                            } else {
                                hasPassword = true;
                                SpUtil.save(SettingFragment.PATTERN_LOCK_PSW, PatternLockUtils.patternToString(patternLockView, patternLockView.getPattern()));
                                patternDialog.dismiss();
                                shortSplash.setChecked(false);
                                shortSplash.setEnabled(false);
                            }
                        }

                        @Override
                        public void onCleared() {

                        }
                    });
                });
        patternDialog.show();
    }

    private void refreshCache() {
        String cache = String.format(getString(R.string.set_log_off_hint) + " %s", getDataSize());
        clearCache.setSummary(cache);
    }

    private void secretStepTwo() {
        if (System.currentTimeMillis() - startTime < DURATION * (secretIndex + 1)) {
            if (secretIndex > 2) {
                Log.i("test", "splash " + secretIndex);
                secretIndex++;
            }
        }
        if (secretIndex == 6) {
            if (SpUtil.getBoolean(SECRET_MODE)) {
                SpUtil.save(SECRET_MODE, false);
                secretIndex = 0;
                UiUtils.showSnack(rootView, R.string.secret_mode_closed);
            } else {
                SpUtil.save(SECRET_MODE, true);
                secretIndex = 0;
                UiUtils.showSnackLong(rootView, R.string.secret_mode_opened);
            }
            secretIndex++;
        }
    }

    private void secretStepOne() {
        if (first) {
            startTime = System.currentTimeMillis();
            first = false;
        }
        if (System.currentTimeMillis() - startTime < DURATION * (secretIndex + 1)) {
            if (secretIndex < 3) {
                secretIndex++;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (null == rootView) {
            rootView = super.onCreateView(inflater, container, savedInstanceState);
        }
        return rootView;

    }

    private String getDataSize() {
        File file = App.context.getCacheDir();
        return FileUtils.getDirSize(file);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        switch (key) {
            case LOG_OFF:
                Observable.just(clearCache())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(success -> {
                            if (success) {
                                refreshCache();
                                AppUtil.restartApp(getActivity());
                            } else {
                                UiUtils.showSnackLong(rootView, R.string.clear_cache_failed, R.string.go_setting, v -> AppUtils.getAppDetailsSettings(getActivity()));
                            }
                        });

                break;
            case FEED_BACK:
                sendFeedback();
                break;
        }
        return true;
    }

    private boolean clearCache() {
        ((BaseActivity) getActivity()).getBase().clearAll();
        SpUtil.clear();
        return CleanUtils.cleanExternalCache() && CleanUtils.cleanInternalCache();
    }

    private void sendFeedback() {
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(R.layout.intro_layout).setTitle(R.string.set_feedback)
                .create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();
        EditText editText = dialog.findViewById(R.id.introEt);
        editText.setHint(R.string.feedback_hint);
        editText.setSelection(editText.getText().length());
        Button commit = dialog.findViewById(R.id.commit);
        commit.setOnClickListener(v -> {
            if (editText.getText().length() < 5) {
                UiUtils.showSnack(commit, R.string.say_more);
                return;
            }
            String feedback = editText.getText().toString();
            Bugtags.sendFeedback(feedback + " by " + LoginManager.getMyStringId() + " \nver: " + BuildConfig.VERSION_CODE);
            UiUtils.showSnack(commit, getString(R.string.thx_for_feedback));
            new Handler().postDelayed(dialog::dismiss, 500);
        });
    }

    public void replace(Fragment fragment) {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.fade_in_animator, R.animator.fade_out_animator)
                .replace(R.id.fragmentLayout, fragment)
                .addToBackStack("")
                .commit();
    }

}
