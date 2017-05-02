package com.dante.diary.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dante.diary.R;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static com.dante.diary.base.App.context;

/**
 * loading img encapsulation.
 */
public class Imager {


    public static void loadDefer(final Fragment context, String url, SimpleTarget<Bitmap> target) {
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
        if (checkContext(context)) return;

        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.image_place_holder)
                .animate(R.anim.fade_in)
                .error(R.drawable.error_holder)
                .into(target);
    }

    public static void load(final android.support.v4.app.Fragment fragment, String url, ImageView target) {
        Glide.with(fragment)
                .load(url)
                .crossFade()
                .error(R.drawable.error_holder)
                .into(target);
    }

    public static void load(Context context, int resourceId, ImageView view) {
        Glide.with(context)
                .load(resourceId)
                .into(view);
    }


    public static void load(String url, int animationId, ImageView view) {
        Glide.with(context)
                .load(url)
                .animate(animationId)
                .into(view);
    }

    public static void loadAvatar(Context context, String url, ImageView avatarView) {
        if (checkContext(context)) return;
        Glide.with(context).load(url)
                .bitmapTransform(new CropCircleTransformation(context))
                .into(avatarView);
    }

    private static boolean checkContext(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) {
                return true;
            }
        }
        return false;
    }

}
