package com.dante.diary.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dante.diary.R;
import com.dante.diary.base.App;

/**
 * loading img encapsulation.
 */
public class Imager {


    public static void loadDefer(final Context context, String url, SimpleTarget<Bitmap> target) {
        Glide.with(context)
                .load(url)
                .asBitmap()
                .error(R.drawable.portrait_holder)
                .into(target);
    }

    public static void load(final Context context, String url, ImageView target, RequestListener<String, Bitmap> listener) {
        Glide.with(context)
                .load(url)
                .asBitmap()
                .error(R.drawable.error_holder)
                .animate(R.anim.fade_in)
                .listener(listener)
                .into(target);
    }

    public static void load(final Context context, String url, ImageView target) {
        Glide.with(context)
                .load(url)
                .crossFade()
                .into(target);
    }

    public static void load(Context context, int resourceId, ImageView view) {
        Glide.with(context)
                .load(resourceId)
                .crossFade()
                .into(view);
    }


    public static void load(String url, int animationId, ImageView view) {
        Glide.with(App.context)
                .load(url)
                .animate(animationId)
                .into(view);
    }

}
