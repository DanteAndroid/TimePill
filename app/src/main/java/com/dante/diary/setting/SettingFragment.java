package com.dante.diary.setting;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.utils.CleanUtils;
import com.blankj.utilcode.utils.FileUtils;
import com.dante.diary.R;
import com.dante.diary.base.AboutActivity;
import com.dante.diary.base.App;
import com.dante.diary.base.BaseActivity;
import com.dante.diary.utils.AppUtil;
import com.dante.diary.utils.SpUtil;
import com.dante.diary.utils.UiUtils;

import java.io.File;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * the view in setting activity.
 */
public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    public static final String CLEAR_CACHE = "clear_cache";
    public static final String FEED_BACK = "feedback";
    public static final String SECRET_MODE = "secret_mode";
    public static final String THEME_COLOR = "theme_color";
    public static final String MY_HOME = "my_home";
    public static final String PROFILE = "profile";
    public static final String ABOUT = "about";
    private static final long DURATION = 300;

    private Preference clearCache;
    private Preference feedback;
    private Preference version;
    private SwitchPreference my;
    private Preference about;

    private View rootView;
    private long startTime;
    private boolean first = true;
    private int secretIndex;
    private Preference theme;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        clearCache = findPreference(CLEAR_CACHE);
        feedback = findPreference(FEED_BACK);
        my = (SwitchPreference) findPreference(MY_HOME);
        about = findPreference(ABOUT);
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
        my.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                AppUtil.restartApp();
                return true;
            }
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

    private void refreshCache() {
        String cache = String.format(getString(R.string.set_current_cache) + " %s", getDataSize());
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
            case CLEAR_CACHE:
                Observable.just(clearCache())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(success -> {
                            if (success) {
                                refreshCache();
                                UiUtils.showSnack(rootView, R.string.clear_finished);
                                AppUtil.restartApp();

                            } else {
                                UiUtils.showSnack(rootView, R.string.clear_cache_failed);
                            }
                        });

                break;
            case FEED_BACK:
                sendEmailFeedback();
                break;
        }
        return true;
    }

    private boolean clearCache() {
        ((BaseActivity) getActivity()).base.clearAllDiaries();
        SpUtil.clear();
        return CleanUtils.cleanInternalCache();
    }

    private void sendEmailFeedback() {
        //This is wired, I used ACTION_SENDTO at first
        //but check intent returns unsafe
        //so I change to ACTION_VIEW (like the system do)
        Intent email = new Intent(Intent.ACTION_SENDTO);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            email = new Intent(Intent.ACTION_VIEW);
        }
        if (AppUtil.isIntentSafe(email)) {
            email.setData(Uri.parse("mailto:" + getString(R.string.my_email)));
            email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " Feedback");
            email.putExtra(Intent.EXTRA_TEXT, "Hi，");
            startActivity(email);
        } else {
            UiUtils.showSnack(rootView, R.string.email_not_install);
        }
    }

    public void replace(Fragment fragment) {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.fade_in_animator, R.animator.fade_out_animator)
                .replace(R.id.fragmentLayout, fragment)
                .addToBackStack("")
                .commit();
    }

}
