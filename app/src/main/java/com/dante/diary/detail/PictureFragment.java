package com.dante.diary.detail;


import android.Manifest;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.dante.diary.R;
import com.dante.diary.base.BaseFragment;
import com.dante.diary.base.Constants;
import com.dante.diary.utils.BitmapUtil;
import com.dante.diary.utils.BlurBuilder;
import com.dante.diary.utils.Imager;
import com.dante.diary.utils.Share;
import com.dante.diary.utils.UiUtils;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import ooo.oxo.library.widget.TouchImageView;
import rx.Observable;
import rx.Subscription;

/**
 * A simple {@link Fragment} subclass.
 */
public class PictureFragment extends BaseFragment implements View.OnLongClickListener {

    @BindView(R.id.image)
    TouchImageView imageView;
    private Bitmap bitmap;
    private String url;
    private boolean isGif;


    public static PictureFragment newInstance(String url) {
        PictureFragment fragment = new PictureFragment();
        Bundle args = new Bundle();
        args.putString(Constants.URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    public static PictureFragment newInstance(String url, boolean isGif) {
        PictureFragment fragment = new PictureFragment();
        Bundle args = new Bundle();
        args.putString(Constants.URL, url);
        args.putBoolean("isGif", isGif);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int initStatusBarColor() {
        return android.R.color.transparent;
    }

    @Override
    protected int initLayoutId() {
        return R.layout.fragment_viewer;
    }

    @Override
    protected void initViews() {
        url = getArguments().getString(Constants.URL);
        isGif = getArguments().getBoolean("isGif", false);
        if (isGif) {
            Glide.with(this).asGif().load(url).listener(new RequestListener<GifDrawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                    getActivity().onBackPressed();
                    return false;
                }

                @Override
                public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                    return false;
                }

            }).into(imageView);
            imageView.setDoubleTapEnabled(false);
        } else {
            ViewCompat.setTransitionName(imageView, url);
            load(url);
        }

    }


    @Override
    protected void initData() {
        imageView.setSingleTapListener(() -> Objects.requireNonNull(getActivity()).onBackPressed());
        if (!isGif) imageView.setOnLongClickListener(this);
    }

    private void load(String url) {
        Imager.loadDefer(this, url, new SimpleTarget<Bitmap>() {

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                imageView.setImageResource(R.drawable.error_holder);
                UiUtils.showSnackLong(imageView, R.string.picture_load_fail, R.string.retry,
                        v -> load(url));
            }

            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                imageView.setImageBitmap(resource);
                startTransition();
                bitmap = resource;

            }
        });
    }

    @Override
    public boolean onLongClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String[] items = getResources().getStringArray(R.array.picture_menu);
        builder.setItems(items, (dialog, which) -> {
            if (which == 0) {
                share(bitmap);
            } else if (which == 1) {
                save(bitmap);
            }
        }).setOnDismissListener(dialogInterface -> {
            imageView.setImageBitmap(bitmap);
        }).show();
        return true;
    }

    private void share(final Bitmap bitmap) {
        final RxPermissions permissions = new RxPermissions(getActivity());
        Subscription subscription = permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .map(granted -> {
                    if (granted) {
                        return BitmapUtil.bitmapToUri(bitmap);
                    }
                    return null;
                })
                .compose(applySchedulers())
                .subscribe(uri -> {
                    Share.shareImage(getActivity(), uri);
                });
        compositeSubscription.add(subscription);
    }

    private void save(final Bitmap bitmap) {
        RxPermissions permissions = new RxPermissions(getActivity());
        Subscription subscription = permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .map(granted -> BitmapUtil.writeToFile(bitmap))
                .compose(applySchedulers())
                .subscribe(file -> {
                    if (file != null && file.exists()) {
                        UiUtils.showSnack(rootView, R.string.save_img_success);

                    } else {
                        UiUtils.showSnack(rootView, R.string.save_img_failed);
                    }
                });
        compositeSubscription.add(subscription);
    }

    private void blur(Bitmap bitmap) {
        Subscription subscription = Observable.just(bitmap)
                .map(BlurBuilder::blur)
                .compose(applySchedulers())
                .subscribe(bitmap1 -> {
                    imageView.setImageBitmap(bitmap1);
                });
        compositeSubscription.add(subscription);
    }


}
