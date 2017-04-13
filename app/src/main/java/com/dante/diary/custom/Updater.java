package com.dante.diary.custom;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;

import com.dante.diary.BuildConfig;
import com.dante.diary.R;
import com.dante.diary.model.AppInfo;
import com.dante.diary.net.API;
import com.dante.diary.net.AppApi;
import com.dante.diary.net.NetService;
import com.dante.diary.utils.AppUtil;
import com.dante.diary.utils.SpUtil;
import com.tbruyelle.rxpermissions.RxPermissions;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Update app helper.
 */

public class Updater {
    public static final String SHARE_APP = "share_app";
    private static Subscription subscription;
    private final Activity context;
    private DownloadHelper helper;

    private Updater(Activity context) {
        this.context = context;
    }

    public static Updater getInstance(Activity context) {
        return new Updater(context);

    }

    private void downloadAndInstall(final AppInfo appInfo) {
        subscription = new RxPermissions(context)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .filter(granted -> granted)
                .subscribe(aBoolean -> {
                    String url = appInfo.getApkUrl();

                    helper = new DownloadHelper(context, url);
                    helper.downWithDownloadManager(getApkName(appInfo.getVersion()), getApkName(appInfo.getFormerVersion()));
                });
    }

    public void check() {
        Log.d("test", "check: ");
        NetService.createServiceWithBaseUrl(AppApi.class, API.GITHUB_RAW).getAppInfo()
                .filter(appInfo -> {
                    SpUtil.save(Updater.SHARE_APP, appInfo.getShareApp());
                    return appInfo.getVersionCode() > BuildConfig.VERSION_CODE;//版本有更新
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showDialog, Throwable::printStackTrace);
    }

    private void showDialog(final AppInfo appInfo) {
        boolean needUpdate = appInfo.isForceUpdate();
        new AlertDialog.Builder(context).setTitle(R.string.detect_new_version)
                .setCancelable(!needUpdate)//需要更新就不可取消
                .setMessage(String.format(context.getString(R.string.update_message), appInfo.getMessage()))
                .setPositiveButton(R.string.update, (dialog, which) -> downloadAndInstall(appInfo))
                .setNegativeButton(R.string.go_market, (dialog, which) -> AppUtil.goMarket(context))
                .show();
    }

    private String getApkName(String version) {
        return "timepill_" + version + ".apk";
    }

    public void release() {
        if (helper != null) {
            helper.release();
        }
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }
}
