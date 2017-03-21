package com.dante.diary.utils;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.view.View;

import com.dante.diary.base.Constants;
import com.dante.diary.detail.ViewerActivity;

/**
 * Created by yons on 17/3/21.
 */

public class TransitionHelper {
    public static void startViewer(Activity context, View view, String url) {
        ViewCompat.setTransitionName(view, url);
        Intent intent = new Intent(context.getApplicationContext(), ViewerActivity.class);
        intent.putExtra(Constants.URL, url);
        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(context, view, url);
        ActivityCompat.startActivity(context, intent, options.toBundle());
    }
}
