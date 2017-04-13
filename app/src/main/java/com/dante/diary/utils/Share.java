package com.dante.diary.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.blankj.utilcode.utils.IntentUtils;
import com.dante.diary.R;
import com.dante.diary.base.App;
import com.dante.diary.custom.Updater;


/**
 * Util to share data, make share intent, etc.
 */
public class Share {

    public static void shareText(Context context, String text) {
        context.startActivity(
                Intent.createChooser(IntentUtils.getShareTextIntent(text),
                        context.getString(R.string.share_to)));
    }

    public static void shareImage(Activity context, @NonNull Uri uri) {
        context.startActivity(
                Intent.createChooser(IntentUtils.getShareImageIntent("", uri),
                        context.getString(R.string.share_to)));
    }

    public static String appendUrl(String text) {
        return text + " " + SpUtil.get(Updater.SHARE_APP, App.context.getString(R.string.share_url));
    }
}
