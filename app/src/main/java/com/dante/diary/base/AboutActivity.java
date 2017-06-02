package com.dante.diary.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dante.diary.BuildConfig;
import com.dante.diary.R;
import com.dante.diary.timepill.TimePillActivity;
import com.dante.diary.utils.AppUtil;

import butterknife.BindView;
import top.wefor.circularanim.CircularAnim;

/**
 * about the author and so on.
 */
public class AboutActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "AboutActivity";
    private static final long DURATION = 300;
    @BindView(R.id.versionName)
    TextView versionName;
    @BindView(R.id.donate)
    TextView donate;
    @BindView(R.id.app)
    LinearLayout app;
    private long startTime;
    private int secretIndex;

    @Override
    protected int initLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        versionName.setText(String.format(getString(R.string.version) + " %s(%s)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        donate.setOnClickListener(v -> AppUtil.donate(AboutActivity.this));
        app.setOnClickListener(this);
    }

    private void go() {
        startTime = System.currentTimeMillis();
        if (System.currentTimeMillis() - startTime < DURATION * (secretIndex + 1)) {
            secretIndex++;
            if (secretIndex == 3) {
                CircularAnim.fullActivity(AboutActivity.this, app)
                        .colorOrImageRes(R.color.colorPrimary)
                        .duration(600)
                        .go(() -> startActivity(new Intent(getApplicationContext(), TimePillActivity.class)));
                secretIndex = 0;
            }
        }
    }

    @Override
    public void onClick(View v) {
        go();
    }
}
