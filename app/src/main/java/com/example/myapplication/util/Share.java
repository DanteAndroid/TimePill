package com.example.myapplication.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.blankj.utilcode.util.IntentUtils;
import com.blankj.utilcode.util.Utils;
import com.example.myapplication.R;

import androidx.annotation.NonNull;


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
                Intent.createChooser(IntentUtils.getShareImageIntent(uri),
                        context.getString(R.string.share_to)));
    }

    public static String appendUrl(String text) {
        return text + " " + Utils.getApp().getString(R.string.share_url);
    }
}
