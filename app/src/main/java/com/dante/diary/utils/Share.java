package com.dante.diary.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.blankj.utilcode.utils.IntentUtils;
import com.dante.diary.R;
import com.dante.diary.base.App;


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
        return String.format("%s from: " + App.context.getString(R.string.timepill_url), text);
    }
}
