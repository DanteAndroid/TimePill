package com.dante.diary.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dante.diary.BuildConfig;
import com.dante.diary.R;
import com.dante.diary.custom.Updater;
import com.dante.diary.detail.PictureActivity;
import com.dante.diary.utils.AppUtil;
import com.dante.diary.utils.SpUtil;

import butterknife.BindView;

import static com.dante.diary.base.App.context;

/**
 * about the author and so on.
 */
public class AboutActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "AboutActivity";
    private static final long DURATION = 300;
    private static final String EGG_URL = "http://pic62.nipic.com/file/20150321/10529735_111347613000_2.jpg";
    @BindView(R.id.versionName)
    TextView versionName;
    @BindView(R.id.donate)
    TextView donate;
    @BindView(R.id.app)
    LinearLayout app;
    @BindView(R.id.icon)
    ImageView icon;
    private long startTime;
    private int secretIndex;

    @Override
    protected int initLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        getToolbar().setNavigationOnClickListener(v -> onBackPressed());

        versionName.setText(String.format(getString(R.string.version) + " %s(%s)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        donate.setOnClickListener(v -> AppUtil.donate(AboutActivity.this));
        app.setOnClickListener(this);
    }

    private void go() {
        startTime = System.currentTimeMillis();
        if (System.currentTimeMillis() - startTime < DURATION * (secretIndex + 1)) {
            secretIndex++;
            if (secretIndex == 3) {
                viewGif();
                secretIndex = 0;
            }
        }
    }

    private void viewGif() {
        String url = SpUtil.getString(Updater.EGG_URL);
        Intent intent = new Intent(context.getApplicationContext(), PictureActivity.class);
        intent.putExtra(Constants.URL, url.isEmpty() ? EGG_URL : url);
        intent.putExtra("isGif", url.contains("gif"));
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        go();
    }

}
