package com.dante.diary.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.dante.diary.BuildConfig;
import com.dante.diary.R;
import com.dante.diary.utils.AppUtil;
import com.dante.diary.utils.UiUtils;

import butterknife.BindView;

/**
 * about the author and so on.
 */
public class AboutActivity extends BaseActivity {
    private static final String TAG = "AboutActivity";
    @BindView(R.id.versionName)
    TextView versionName;
    @BindView(R.id.donate)
    TextView donate;

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
    }

    @Override
    public void startActivity(Intent intent) {
        if (AppUtil.isIntentSafe(intent)) {
            super.startActivity(intent);
        } else {
            UiUtils.showSnack(versionName, R.string.email_not_install);
        }
    }

}
