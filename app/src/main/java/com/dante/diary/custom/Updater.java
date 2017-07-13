package com.dante.diary.custom;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.text.TextUtils;

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
    public static final String SHOULD_SHOW_UPDATE = "shouldShow";
    public static final String SHOULD_SHOW_ANNOUNCEMENT = "shouldShowAnnouncement";
    public static final String EGG_URL = "egg_url";
    private static Subscription subscription;
    private final Activity context;
    private DownloadHelper helper;
    private String formerVer;

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
        NetService.createServiceWithBaseUrl(AppApi.class, API.GITHUB_RAW).getAppInfo()
                .filter(appInfo -> {
                    showAnnouncement(appInfo);
                    SpUtil.save(Updater.SHARE_APP, appInfo.getShareApp());
                    SpUtil.save(Updater.EGG_URL, appInfo.getEggUrl());
                    return appInfo.getVersionCode() > BuildConfig.VERSION_CODE;//版本有更新
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(appInfo -> {
                    formerVer = appInfo.getFormerVersion();
                    boolean shouldShowUpdate = SpUtil.getBoolean(appInfo.getVersion() + SHOULD_SHOW_UPDATE, true);
                    if (shouldShowUpdate) {
                        showDialog(appInfo);
                    }
                }, Throwable::printStackTrace);
    }

    private void showAnnouncement(AppInfo appInfo) {
        if (TextUtils.isEmpty(appInfo.getAnnouncement())
                || !SpUtil.getBoolean(appInfo.getAnnouncement() + SHOULD_SHOW_ANNOUNCEMENT, true)) {
            return;
        }

        context.runOnUiThread(() -> new AlertDialog.Builder(context)
                .setMessage(appInfo.getAnnouncement())
                .setPositiveButton(R.string.got_it,
                        (dialog, which) -> SpUtil.save(appInfo.getAnnouncement() + SHOULD_SHOW_ANNOUNCEMENT, false))
                .show());

    }

    private void showDialog(final AppInfo appInfo) {
        boolean needUpdate = appInfo.isForceUpdate();
        new AlertDialog.Builder(context).setTitle(R.string.detect_new_version)
                .setCancelable(!needUpdate)//需要更新就不可取消
                .setMessage(String.format(context.getString(R.string.update_message), appInfo.getMessage()))
                .setPositiveButton(R.string.update, (dialog, which) -> downloadAndInstall(appInfo))
                .setNeutralButton(R.string.go_market, (dialog, which) -> AppUtil.goMarket(context))
                .setNegativeButton(R.string.dont_hint_update,
                        (dialog, which) -> SpUtil.save(appInfo.getVersion() + SHOULD_SHOW_UPDATE, false))
                .show();
    }

    private String getApkName(String version) {
        return "timepill_" + version + ".apk";
    }

    public void release() {
        SpUtil.remove(formerVer + SHOULD_SHOW_UPDATE);
        if (helper != null) {
            helper.release();
        }
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }
}
