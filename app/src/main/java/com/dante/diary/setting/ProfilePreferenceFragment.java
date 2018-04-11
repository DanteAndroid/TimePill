package com.dante.diary.setting;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.blankj.utilcode.utils.ClipboardUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dante.diary.R;
import com.dante.diary.custom.PickPictureActivity;
import com.dante.diary.login.LoginManager;
import com.dante.diary.model.DataBase;
import com.dante.diary.model.User;
import com.dante.diary.net.NetService;
import com.dante.diary.utils.UiUtils;

import java.io.File;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static android.app.Activity.RESULT_OK;
import static com.dante.diary.custom.PickPictureActivity.REQUEST_PICK_PICTURE;

/**
 * Created by yons on 17/3/29.
 */

public class ProfilePreferenceFragment extends PreferenceFragment {
    private static final String TAG = "ProfilePreferenceFragme";
    private static final String NICKNAME = "nickname";
    private static final String INTRO = "intro";
    private static final String GENDER = "gender";
    private static final String AVATAR = "avatar";
    private static final String USER_ID = "user_id";
    private CompositeSubscription subscription = new CompositeSubscription();
    private Preference nickname;
    private ListPreference gender;
    private Preference intro;
    private String nickName;
    private String introduction;
    private DataBase base;
    private User user;
    private Preference avatar;
    private File photoFile;
    private Preference userId;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_PICTURE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                String path = data.getStringExtra("path");
                retrievePicture(uri, path);
            } else if (resultCode == PickPictureActivity.RESULT_FAILED) {
                UiUtils.showSnack(getView(), getString(R.string.fail_read_pictures));
            }

        }
    }

    private void retrievePicture(Uri uri, String path) {
        photoFile = new File(path);
        LoginManager.getApi().setUserIcon(NetService.createMultiPart("icon", photoFile))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> {
                    base.save(user);
                    UiUtils.showSnack(getView(), getString(R.string.success_to_upload_avatar));
                    loadAvatar(user);

                }, throwable -> UiUtils.showSnack(getView(), getString(R.string.fail_to_upload_avatar)));
    }

    private void loadAvatar(User user) {
        Glide.with(getActivity()).load(user.getAvatarUrl())
                .crossFade()
                .listener(new RequestListener<String, GlideDrawable>() {

                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        findPreference("avatar").setIcon(resource);
                        return true;
                    }
                }).into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_profile);
        setHasOptionsMenu(true);
        nickname = findPreference(NICKNAME);
        userId = findPreference(USER_ID);
        intro = findPreference(INTRO);
        gender = (ListPreference) findPreference(GENDER);
        avatar = findPreference(AVATAR);
        base = DataBase.getInstance();
        user = base.findUser(LoginManager.getMyId());

        initUserInfo();

        nickname.setOnPreferenceChangeListener((preference, newValue) -> {
            nickName = (String) newValue;
            if (nickName.equals(user.getName())) return false;
            subscription.add(update());
            return true;
        });

        avatar.setOnPreferenceClickListener(preference -> {
            startActivityForResult(new Intent(getActivity(), PickPictureActivity.class), REQUEST_PICK_PICTURE);
            return true;
        });
        gender.setOnPreferenceChangeListener((preference, newValue) -> {
            String g = (String) newValue;
            switch (g) {
                case "m":
                    if (introduction.startsWith("男。")) {
                        return true;
                    } else if (introduction.startsWith("女。")) {
                        introduction = introduction.replace("女。", "男。");
                    } else {
                        introduction = "男。" + introduction;
                    }
                    update();
                    break;
                case "w":
                    if (introduction.startsWith("女。")) {
                        return true;
                    } else if (introduction.startsWith("男。")) {
                        introduction = introduction.replace("男。", "女。");
                    } else {
                        introduction = "女。" + introduction;
                    }
                    update();
                    break;
                default:
                    if (introduction.startsWith("男。") || introduction.startsWith("女。")) {
                        introduction = introduction.substring(2, introduction.length());
                    } else {
                        return true;
                    }
                    update();
                    break;
            }
            return true;
        });

        userId.setOnPreferenceClickListener(preference -> {
            if (preference.getSummary() != null) {
                ClipboardUtils.copyText(preference.getSummary());
                UiUtils.showSnack(getView(), getString(R.string.id_has_been_copied));
            }
            return true;
        });

    }

    private void initUserInfo() {
        if (user != null) {
            userId.setSummary(String.valueOf(user.getId()));
            nickName = user.getName();
            introduction = user.getIntro() == null ? "" : user.getIntro();
            nickname.setSummary(nickName);
            nickname.setOnPreferenceClickListener(preference -> {
                ((EditTextPreference) preference).getEditText().setText(user.getName());
                ((EditTextPreference) preference).getEditText().selectAll();
                return true;
            });

            loadAvatar(user);
            intro.setSummary(introduction);
            intro.setOnPreferenceClickListener(preference -> {
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setView(R.layout.intro_layout).setTitle(R.string.pref_intro)
                        .create();
                dialog.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.show();
                EditText editText = dialog.findViewById(R.id.introEt);
                editText.setText(introduction);
                editText.setSelection(editText.getText().length());
                Button commit = dialog.findViewById(R.id.commit);
                commit.setOnClickListener(v -> {
//                        if (editText.getText().length() == 0) {
//                            UiUtils.showSnack(commit, R.string.say_more);
//                            return;
//                        }
                    dialog.dismiss();
                    introduction = editText.getText().toString();
                    update();
                });
                return true;
            });

//            intro.setOnPreferenceClickListener(preference -> {
//                ((EditTextPreference) preference).getEditText().setText(user.getIntro());
//                ((EditTextPreference) preference).getEditText().append("");
//                return true;
//            });
        }
    }

    private Subscription update() {
        return LoginManager.getApi().updateUserInfo(nickName, introduction)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user1 -> {
                    base.save(user1);
                    ProfilePreferenceFragment.this.user = user1;
                    initUserInfo();
                    UiUtils.showSnack(getView(), getString(R.string.update_user_success));

                }, throwable -> UiUtils.showSnackLong(getView(), R.string.update_user_failed, R.string.retry, v -> update()));
    }

    @Override
    public void onDestroyView() {
        subscription.unsubscribe();
        super.onDestroyView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(getActivity(), SettingActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
