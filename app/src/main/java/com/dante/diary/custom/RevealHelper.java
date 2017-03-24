package com.dante.diary.custom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;

import static android.view.ViewAnimationUtils.createCircularReveal;

/**
 * This helper makes startButton moves to center of revealView then reveal the view & hide the view before.
 */

public class RevealHelper {
    private static final String TAG = "RevealHelper";
    private static final int BUTTON_TRANSITION_DURATION = 250;
    private static final int REVEAL_DURATION = 400;
    private Activity activity;
    private View startButton;
    private View revealView;
    private View hideView;
    private float finalRadius;
    private float pixelDensity;
    private int revealX;
    private int revealY;
    private Animator.AnimatorListener onRevealEnd;
    private Animator.AnimatorListener onUnrevealEnd;
    private int hypotenuse;
    private int duration;
    private int fabTransitionDuration;

    private RevealHelper(Activity activity) {
        this.activity = activity;
    }

    public static RevealHelper with(Activity activity) {
        return new RevealHelper(activity);
    }

    public RevealHelper button(final View startButton) {
        this.startButton = startButton;
        return this;
    }

    public RevealHelper reveal(View revealView) {
        if (activity == null) {
            throw new NullPointerException("Activity cannot be null, call with(Activity) first");
        }
        this.revealView = revealView;
        pixelDensity = activity.getResources().getDisplayMetrics().density;
        return this;
    }

    public RevealHelper revealDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public RevealHelper build() {
        if (revealView == null) {
            throw new NullPointerException("Reveal view cannot be null, call reveal(View) first");
        }
        revealView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                revealX = revealView.getWidth() / 2;
                revealY = revealView.getHeight() / 2;
                Log.d(TAG, "onGlobalLayout: " + revealX);
                hypotenuse = (int) Math.hypot(revealX, revealY);
                revealView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        if (startButton == null) {
            //if no button, just reveal the revealView from center
            revealFromCenter();
            return this;
        }

        startButton.setOnClickListener(view -> {
            Log.d(TAG, " OnClickListener: animate");

     /*
     MARGIN = 16dp
     FAB_BUTTON_RADIUS = 28 dp
     */
            startButton.animate()
                    .translationX(-(revealX - (16 + 28) * pixelDensity))
                    .translationY(-(revealY - (16 + 28) * pixelDensity))
//                        .setInterpolator(new DecelerateInterpolator())
                    .setDuration(fabTransitionDuration > 0 ? fabTransitionDuration : BUTTON_TRANSITION_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            revealFromCenter();
                        }
                    });

        });
        return this;
    }

    public RevealHelper onRevealEnd(Animator.AnimatorListener onRevealEnd) {
        this.onRevealEnd = onRevealEnd;
        return this;
    }

    public RevealHelper onUnrevealEnd(Animator.AnimatorListener onUnrevealEnd) {
        this.onUnrevealEnd = onUnrevealEnd;
        return this;
    }

    public RevealHelper hide(View hideView) {
        this.hideView = hideView;
        return this;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void revealFromCenter() {
        Log.d(TAG, "revealFromCenter: " + revealX);
        Animator reveal = createCircularReveal(revealView, revealX, revealY, 28 * pixelDensity, hypotenuse);
        reveal.setDuration(duration > 0 ? duration : REVEAL_DURATION)
                .addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        View.OnClickListener onClickListener = v -> unreveal();
                        revealView.setOnClickListener(onClickListener);
                        if (onRevealEnd != null) {
                            onRevealEnd.onAnimationEnd(animation);
                        }
                    }
                });
        revealView.setVisibility(View.VISIBLE);

        if (startButton != null) {
            startButton.setVisibility(View.GONE);
        }
        if (hideView != null) {
            hideView.setVisibility(View.GONE);
        }
//        reveal.setInterpolator(new AccelerateInterpolator());
        reveal.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void unreveal() {
        if (revealView == null) {
            throw new NullPointerException("Reveal view cannot be null, call reveal(View) first");
        }

        Animator animator = ViewAnimationUtils.createCircularReveal(revealView, revealX, revealY,
                hypotenuse, 28 * pixelDensity);

        animator.setDuration(REVEAL_DURATION)
                .addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        revealView.setVisibility(View.GONE);
                        if (startButton != null) {
                            startButton.setVisibility(View.VISIBLE);
                            startButton.animate()
                                    .translationX(0)
                                    .translationY(0)
                                    .setListener(null);
                        }
                        if (onUnrevealEnd != null) {
                            onUnrevealEnd.onAnimationEnd(animation);
                        }
                    }
                });
        animator.setInterpolator(new DecelerateInterpolator());
        if (hideView != null) {
            hideView.setVisibility(View.VISIBLE);
        }
        animator.start();

    }


    public RevealHelper buttonTransitionDuration(int i) {
        fabTransitionDuration = i;
        return this;
    }
}
