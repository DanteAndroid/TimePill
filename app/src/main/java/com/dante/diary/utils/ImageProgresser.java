package com.dante.diary.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

/**
 * Created by yons on 17/3/16.
 */

public class ImageProgresser {

    /**
     * 给 ImageView 上层添加一个相对布局
     * 然后在此相对布局中心加上 Progressbar
     *
     * @param view 需要在中间添加 Progress 的view
     * @return 该 ProgressBar，可以用于加载后的隐藏与再次显示
     */
    @NonNull
    public static ProgressBar attachProgress(@NonNull View view) {
        return attachProgress(view, null);
    }

    @NonNull
    public static ProgressBar attachProgress(@NonNull View view, ProgressBar progressBar) {
        return attachProgress(view, progressBar, 0, 0, false);
    }

    /**
     * 图片还未加载完成时，需要调用此方法
     * 传入图片加载完成后的宽高（单位dp）
     * 否则会出现图片和Progress都没显示的问题
     *
     * @param view        需要添加Progress的view
     * @param widthInDp       要添加Progress的view（图片没加载完）的宽（单位dp）
     * @param heightInDp      要添加Progress的view（图片没加载完）的高（单位dp）
     * @return 自定义的Progressbar，方便再次调用隐藏
     */
    @NonNull
    public static ProgressBar attachProgress(@NonNull View view, int widthInDp, int heightInDp) {
        return attachProgress(view, null, widthInDp, heightInDp, true);
    }

    /**
     * 图片还未加载完成时，需要调用此方法
     * 传入图片加载完成后的宽高（单位px）
     * 否则会出现图片和Progress都没显示的问题
     *
     * @param view        需要添加Progress的view
     * @param widthInPx       要添加Progress的view（图片没加载完）的宽（单位px）
     * @param heightInPx      要添加Progress的view（图片没加载完）的高（单位px）
     * @return 自定义的Progressbar，方便再次调用隐藏
     */
    @NonNull
    public static ProgressBar attachProgressInPx(@NonNull View view, int widthInPx, int heightInPx) {
        return attachProgress(view, null, widthInPx, heightInPx, false);
    }


    @NonNull
    private static ProgressBar attachProgress(@NonNull View view, ProgressBar progressBar, int width, int height, boolean isInDp) {
        if (progressBar == null) {
            progressBar = new ProgressBar(view.getContext());
        }
        //给 ImageView 上层添加一个 ProgressBar
        RelativeLayout layout = new RelativeLayout(view.getContext());
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params instanceof LinearLayout.LayoutParams) {
            throw new UnsupportedOperationException("Please make sure your view's parent layout is not LinearLayout." +
                    "(You can simply add a FrameLayout to wrap your view)");
        }
        if (isInDp) {
            width = dpToPx(view.getContext(), width);
            height = dpToPx(view.getContext(), height);
        }
        params.width = width == 0 ? view.getWidth() : width;
        params.height = height == 0 ? view.getHeight() : height;

        layout.setLayoutParams(params);
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        p.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar.setLayoutParams(p);
        layout.addView(progressBar);
        ((ViewGroup) view.getParent()).addView(layout);
        return progressBar;
    }

    public static int dpToPx(Context context, int dp) {
        final float scale = context.getApplicationContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }


}
