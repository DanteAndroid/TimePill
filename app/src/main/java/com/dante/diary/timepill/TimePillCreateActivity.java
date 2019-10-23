package com.dante.diary.timepill;

import android.Manifest;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;
import com.blankj.utilcode.utils.ClipboardUtils;
import com.dante.diary.R;
import com.dante.diary.base.BaseActivity;
import com.dante.diary.base.Constants;
import com.dante.diary.custom.RecordHelper;
import com.dante.diary.custom.RecordView;
import com.dante.diary.custom.Updater;
import com.dante.diary.login.LoginManager;
import com.dante.diary.utils.AppUtil;
import com.dante.diary.utils.DateUtil;
import com.dante.diary.utils.ImageProgresser;
import com.dante.diary.utils.SpUtil;
import com.dante.diary.utils.UiUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import rx.Subscription;

import static com.dante.diary.edit.EditDiaryActivity.DIARY_CONTENT_TEXT_LIMIT;
import static com.dante.diary.timepill.TimePillOpenActivity.PREFIX;
import static com.dante.diary.timepill.TimePillOpenActivity.SUFFIX;

/**
 * Created by yons on 17/5/11.
 */

public class TimePillCreateActivity extends BaseActivity {
    private static final String TAG = "TimePillCreateActivity";
    private static final int RECORD_LIMIT = 5;
    private static String keyString;
    @BindView(R.id.content)
    EditText contentET;
    @BindView(R.id.keyLayout)
    LinearLayout keyLayout;
    @BindView(R.id.key)
    TextView key;
    @BindView(R.id.copy)
    Button copy;
    @BindView(R.id.name)
    TextInputEditText nameET;
    @BindView(R.id.openTime)
    TextInputEditText openTimeET;
    @BindView(R.id.hint)
    TextInputEditText hintET;
    @BindView(R.id.recordStatus)
    TextView recordStatus;
    @BindView(R.id.soundLayout)
    LinearLayout soundLayout;
    @BindView(R.id.textLayout)
    TextInputLayout textLayout;
    @BindView(R.id.root)
    LinearLayout root;
    @BindView(R.id.record)
    RecordView record;

    private String name;
    private Date openTime;
    private String hint;
    private String content;
    private boolean isSending;

    public static String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-";
        StringBuilder salt = new StringBuilder();
        Random random = new Random();
        while (salt.length() < TimePillOpenActivity.KEY_LENGTH) { // length of the random string.
            int index = (int) (random.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        salt.insert(0, PREFIX).append(SUFFIX);
        return salt.toString();
    }

    @Override
    protected int initLayoutId() {
        return R.layout.activity_create_timepill;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        initEditText();
    }

    private void initEditText() {
        String draft = SpUtil.getString("tp_draft");
        if (!draft.isEmpty()) {
            contentET.setText(draft);
            contentET.setSelection(contentET.getText().length());
            SpUtil.remove("tp_draft");
            UiUtils.showSnack(contentET, getString(R.string.draft_restored));
        }

        nameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                name = s.toString();
            }
        });
        hintET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                hint = s.toString();
            }
        });

        openTime = DateUtil.nextWeekDateOfToday();
        openTimeET.setText(DateUtil.getDisplayDayAndTime(openTime));
        openTimeET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                openTime = DateUtil.parseStandardDate(s.toString());
            }
        });
        contentET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                content = s.toString().trim();
                if (content.length() <= DIARY_CONTENT_TEXT_LIMIT) {

                } else {
                    SpUtil.save("tp_draft", content);
                    if (!SpUtil.getBoolean("t_type_hint")) {
                        SpUtil.save("t_type_hint", true);
                        UiUtils.showSnack(contentET, R.string.type_hint);
                    }
                }
                invalidateOptionsMenu();
            }
        });
        record.addRecordCallback(new RecordView.RecordCallback() {
            @Override
            public void startRecord() {
                RxPermissions permissions = new RxPermissions(TimePillCreateActivity.this);
                Subscription subscription = permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                        .map(granted -> {
                            if (granted) {
                                if (RecordHelper.getRecordFile() != null) {
                                    TimePillCreateActivity.this.runOnUiThread(() -> new AlertDialog.Builder(TimePillCreateActivity.this)
                                            .setTitle(R.string.record_new)
                                            .setMessage(R.string.record_overwrite_hint)
                                            .setNegativeButton(R.string.nope, (d, which) -> record.endRecord())
                                            .setPositiveButton(android.R.string.ok,
                                                    (dialog, which) -> {
                                                        RecordHelper.clearCache();
                                                        RecordHelper.record();
                                                    })
                                            .show());
                                } else {
                                    Log.d(TAG, "startRecord: ");
                                    RecordHelper.record();
                                }
                            }
                            return RecordHelper.getRecordFile();
                        })
                        .compose(applySchedulers())
                        .subscribe(file -> {
                            if (file != null && file.exists()) {
//                                ToastUtils.showShortToast(R.string.record_start);
                            } else {
                                UiUtils.showSnack(record, R.string.record_failed);
                                record.endRecord();
                            }
                        }, throwable -> Log.e(TAG, "error: " + throwable.getMessage()));
                getCompositeSubscription().add(subscription);

            }

            @Override
            public void endRecord() {
                RecordHelper.stopRecord();
                new Handler().postDelayed(() -> {
                    Log.d(TAG, "endRecord: " + RecordHelper.getDuration());
                    if (RecordHelper.getDuration() <= RECORD_LIMIT) {
                        recordStatus.setText(R.string.record_too_short);
                    } else {
                        int m = RecordHelper.getDuration() / 60;
                        int s = RecordHelper.getDuration() % 60;
                        Log.d(TAG, "duration: " + RecordHelper.getDuration() + " " + m + " min" + s + " s");
                        recordStatus.setText(String.format(Locale.getDefault(), getString(R.string.record_length), m, s));
                        recordStatus.setOnLongClickListener(v -> {
                            new AlertDialog.Builder(TimePillCreateActivity.this)
                                    .setMessage(R.string.remove_record)
                                    .setNegativeButton(R.string.nope, null)
                                    .setPositiveButton(android.R.string.ok,
                                            (d, which) -> {
                                                RecordHelper.clearCache();
                                                recordStatus.setVisibility(View.GONE);
                                            })
                                    .show();
                            return true;
                        });
                    }
                    recordStatus.setVisibility(View.VISIBLE);
                    recordStatus.setOnClickListener(v -> RecordHelper.playAudio(RecordHelper.getRecordFile()));
                }, 600);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_send) {
            send();
        }
        return super.onOptionsItemSelected(item);
    }

    private void send() {
        if (openTime == null) {
            UiUtils.showSnack(contentET, getString(R.string.input_correct_opentime));
            return;
        }
        if (openTime.before(DateUtil.tomorrowDate())) {
            UiUtils.showSnack(contentET, R.string.opentime_too_early);
            return;
        }
        if (!isRecordValid() && content.length() < DIARY_CONTENT_TEXT_LIMIT) {
            UiUtils.showSnack(contentET, R.string.say_more);
            return;
        }

        if (isSending) {
            return;
        }

        isSending = true;
        ProgressBar progressBar = ImageProgresser.attachProgress(contentET);
        AVObject pill = new AVObject(Constants.TIME_PILL);
        keyString = getSaltString();
        pill.put(Constants.KEY, keyString);
        pill.put(Constants.NAME, name);
        pill.put(Constants.OPEN_TIME, openTime);
        pill.put(Constants.TP_USER, LoginManager.getMyId());
        pill.put(Constants.HINT, hint);
        pill.put(Constants.CONTENT, content);
        if (isRecordValid()) {
            try {
                AVFile f = AVFile.withFile("record.mp3", RecordHelper.getRecordFile());
                f.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            pill.put(Constants.RECORD, f.getUrl());
                            Log.d(TAG, "save record file " + pill.getObjectId() + " " + f.getUrl());
                            pill.saveEventually();
                        }

                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        pill.saveEventually(new SaveCallback() {
            @Override
            public void done(AVException e) {
                isSending = false;
                progressBar.setVisibility(View.GONE);
                if (e == null) {
                    ClipboardUtils.copyText(generateTemplate(keyString));
                    Toast.makeText(TimePillCreateActivity.this, R.string.timepill_created, Toast.LENGTH_SHORT).show();
                    SpUtil.remove("tp_draft");
                    initKeyLayout();
                } else {
                    Log.e(TAG, "done: " + e.getMessage());
                    UiUtils.showSnackLong(contentET, getString(R.string.timepill_create_failed) + e.getMessage());
                }
            }
        });
    }

    private boolean isRecordValid() {
        return RecordHelper.getRecordFile() != null && RecordHelper.getDuration() > RECORD_LIMIT;
    }

    private void initKeyLayout() {
        contentET.setEnabled(false);
        keyLayout.setVisibility(View.VISIBLE);
        keyLayout.animate().scaleX(1).scaleY(1).start();
        key.setText(keyString);
        copy.setOnClickListener(v -> {
            ClipboardUtils.copyText(generateTemplate(keyString));
            UiUtils.showSnackLong(key, R.string.timepill_key_copied, R.string.go_wechat, v1 -> AppUtil.goWechat());
        });
    }

    private String generateTemplate(String keyString) {
        return String.format(getString(R.string.I_created_a_timepill) + SpUtil.getString(Updater.SHARE_APP), keyString);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean enabled = !TextUtils.isEmpty(content)
                && !TextUtils.isEmpty(name)
                && !TextUtils.isEmpty(hint);

        MenuItem item = menu.findItem(R.id.action_send);
        Drawable resIcon = ContextCompat.getDrawable(this, R.drawable.ic_send_white_36px);
        if (!enabled) {
            resIcon.mutate().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        }
        item.setEnabled(enabled);
        item.setIcon(resIcon);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_send, menu);
        return true;
    }

}
