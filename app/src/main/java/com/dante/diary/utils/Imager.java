package com.dante.diary.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dante.diary.R;

import androidx.fragment.app.Fragment;

/**
 * loading img encapsulation.
 */
public class Imager {


    public static void loadDefer(final Fragment context, String url, SimpleTarget<Bitmap> target) {
        Glide.with(context)
                .asBitmap()
                .load(url)
                .error(R.drawable.portrait_holder)
                .into(target);
    }

    public static void load(final Context context, String url, ImageView target, RequestListener<Bitmap> listener) {
        Glide.with(context)
                .asBitmap()
                .load(url)
                .error(R.drawable.error_holder)
                .transition(BitmapTransitionOptions.withCrossFade())
//                .animate(R.anim.fade_in)
                .listener(listener)
                .into(target);
    }

    public static void load(final Context context, String url, ImageView target) {
        if (checkContext(context)) return;

        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.image_place_holder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.error_holder)
                .into(target);
    }

    public static void load(final Fragment fragment, String url, ImageView target) {
        Glide.with(fragment)
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.error_holder)
                .into(target);
    }

    public static void load(Context context, int resourceId, ImageView view) {
        Glide.with(context)
                .load(resourceId)
                .into(view);
    }



    public static void loadAvatar(Context context, String url, ImageView avatarView) {
        if (checkContext(context)) return;
        Glide.with(context).load(url)
                .circleCrop()
                .into(avatarView);
    }

    private static boolean checkContext(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return activity.isFinishing() || activity.isDestroyed();
        }
        return false;
    }

}
