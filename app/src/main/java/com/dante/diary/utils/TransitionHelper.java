package com.dante.diary.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
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

    public static void adjustTransition(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.transition.Fade fade = new android.transition.Fade();
//            fade.excludeTarget(R.id.appBar, true);
            fade.excludeTarget(android.R.id.statusBarBackground, true);
            fade.excludeTarget(android.R.id.navigationBarBackground, true);

            context.getWindow().setEnterTransition(fade);
            context.getWindow().setExitTransition(fade);
        }
    }
}
