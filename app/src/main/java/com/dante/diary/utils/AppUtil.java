package com.dante.diary.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.blankj.utilcode.utils.ClipboardUtils;
import com.dante.diary.BuildConfig;
import com.dante.diary.R;
import com.dante.diary.base.Constants;
import com.dante.diary.main.MainActivity;

import java.util.List;

import moe.feng.alipay.zerosdk.AlipayZeroSdk;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.dante.diary.base.App.context;

/**
 * Created by Dante on 2016/2/19.
 */
public class AppUtil {

    public static String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    public static void openAppInfo(Context context) {
        //redirect user to app Settings
        Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getApplicationContext().getPackageName()));
        context.startActivity(i);
    }

    public static boolean isIntentSafe(Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return activities.size() > 0;
    }

    public static void goMarket(Activity activity) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
            activity.startActivity(intent);
        } catch (android.content.ActivityNotFoundException anfe) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("www.coolapk.com/apk/" + BuildConfig.APPLICATION_ID));
            activity.startActivity(intent);
        }
    }

    public static void donate(Activity activity) {
        if (AlipayZeroSdk.hasInstalledAlipayClient(activity.getApplicationContext())) {
            AlipayZeroSdk.startAlipayClient(activity, Constants.ALI_PAY);
        } else {
            UiUtils.showSnackLong(activity.getWindow().getDecorView(), R.string.alipay_not_found);
            ClipboardUtils.copyText(activity.getString(R.string.wechat));
        }
    }

    public static void restartApp(Activity activity) {
        activity.finishAndRemoveTask();
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }
}
