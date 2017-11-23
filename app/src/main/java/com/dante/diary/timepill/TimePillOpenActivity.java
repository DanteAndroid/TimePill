package com.dante.diary.timepill;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.GetCallback;
import com.dante.diary.R;
import com.dante.diary.base.BaseActivity;
import com.dante.diary.base.Constants;
import com.dante.diary.custom.RecordHelper;
import com.dante.diary.custom.RecordView;
import com.dante.diary.utils.DateUtil;
import com.dante.diary.utils.UiUtils;

import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import hugo.weaving.DebugLog;

/**
 * Created by yons on 17/5/11.
 */

public class TimePillOpenActivity extends BaseActivity {
    public static final String PREFIX = "#tpk-";
    public static final String SUFFIX = "#";
    public static final int KEY_LENGTH = 10;
    private static final String TAG = "TimePillOpenActivity";
    private static String key = "";
    @BindView(R.id.wroteAt)
    TextView wroteAt;
    @BindView(R.id.timePillContent)
    TextView timePillContent;
    @BindView(R.id.name)
    TextView nameTV;
    @BindView(R.id.record)
    RecordView record;
    @BindView(R.id.recordStatus)
    TextView recordStatus;
    @BindView(R.id.soundLayout)
    LinearLayout soundLayout;
    private String name;
    private Date openTime;
    private String hint;
    private String content;
    private String recordUrl;

    public static boolean isValid(String content) {
        if (!TextUtils.isEmpty(content) && content.length() >= KEY_LENGTH) {
            if (content.contains(PREFIX)) {
                return true;
            }
        }
        return false;
    }

    @DebugLog
    public static String retrieveKey(String s) {
        if (s.contains(PREFIX)) {
            return s.substring(s.indexOf(PREFIX), s.lastIndexOf(SUFFIX) + 1);
        }
        return s;
    }


    @Override
    protected int initLayoutId() {
        return R.layout.activity_time_pill_open;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        if (getIntent() != null) {
            key = getIntent().getStringExtra(Constants.KEY);
            fetchKey();
        }
    }

    private void fetchKey() {
        if (isValid(key)) {
            AVQuery<AVObject> query = new AVQuery<>(Constants.TIME_PILL);
            query.whereEqualTo(Constants.KEY, retrieveKey(key));
            query.getFirstInBackground(new GetCallback<AVObject>() {
                @Override
                public void done(AVObject object, AVException e) {
                    if (object == null) {
                        UiUtils.showSnack(wroteAt, getString(R.string.cant_find_timepill) + key);
                    } else {
                        fetchTimePill(object);
                    }
                }
            });
        } else {
            UiUtils.showSnackLong(getToolbar(), R.string.invalid_timepill_key);
        }
    }

    private void fetchTimePill(AVObject avObject) {
        name = avObject.getString(Constants.NAME);
        openTime = avObject.getDate(Constants.OPEN_TIME);
        hint = avObject.getString(Constants.HINT);
        content = avObject.getString(Constants.CONTENT);
        recordUrl = avObject.getString(Constants.RECORD);
        record.addRecordCallback(new RecordView.RecordCallback() {
            @Override
            public void startRecord() {
                int seconds = RecordHelper.playAudio(recordUrl, record);
                int m = seconds / 60;
                int s = seconds % 60;
                Log.d(TAG, "play duration: " + seconds + " " + m + " min" + s + " s");
                recordStatus.setVisibility(View.VISIBLE);
                recordStatus.setText(String.format(Locale.getDefault(), getString(R.string.record_length), m, s));
            }

            @Override
            public void endRecord() {
                RecordHelper.stopPlay();
            }
        });

        if (openTime.after(new Date())) {
            wroteAt.setText(
                    String.format(getString(R.string.time_pill_not_expired), DateUtil.getDisplayDayAndTime(openTime)));
            timePillContent.setText(hint);
        } else {
            nameTV.setText(name);
            nameTV.setVisibility(View.VISIBLE);
            nameTV.animate().alpha(1).start();
            wroteAt.setText(String.format(getString(R.string.xxx_says_at_xxx), DateUtil.getDisplayDayAndTime(openTime)));
            timePillContent.setText(content);
            if (!TextUtils.isEmpty(recordUrl)) {
                soundLayout.setVisibility(View.VISIBLE);
            }
        }
        wroteAt.animate().alpha(1).start();
        timePillContent.animate().alpha(1).start();

    }

}
