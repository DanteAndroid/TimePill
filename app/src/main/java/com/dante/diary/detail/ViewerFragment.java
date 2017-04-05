package com.dante.diary.detail;


import android.Manifest;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.view.View;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dante.diary.R;
import com.dante.diary.base.BaseFragment;
import com.dante.diary.base.Constants;
import com.dante.diary.utils.BitmapUtil;
import com.dante.diary.utils.BlurBuilder;
import com.dante.diary.utils.Imager;
import com.dante.diary.utils.Share;
import com.dante.diary.utils.UiUtils;
import com.tbruyelle.rxpermissions.RxPermissions;

import butterknife.BindView;
import rx.Observable;
import rx.Subscription;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

import static com.dante.diary.base.App.context;

/**
 * A simple {@link Fragment} subclass.
 */
public class ViewerFragment extends BaseFragment implements View.OnLongClickListener, PhotoViewAttacher.OnPhotoTapListener {

    @BindView(R.id.image)
    PhotoView imageView;
    private Bitmap bitmap;
    private String url;


    public static ViewerFragment newInstance(String url) {
        ViewerFragment fragment = new ViewerFragment();
        Bundle args = new Bundle();
        args.putString(Constants.URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setAnimations() {
//        postponeEnterTransition();
    }

    @Override
    protected int initLayoutId() {
        return R.layout.fragment_viewer;
    }

    @Override
    protected void initViews() {
        url = getArguments().getString(Constants.URL);
        ViewCompat.setTransitionName(imageView, url);
        load(url);
    }

    @Override
    protected void initData() {
        imageView.setScaleLevels(1.0f, 1.5f, 2.1f);
        imageView.setOnPhotoTapListener(this);
        imageView.setOnLongClickListener(this);
    }

    private void load(String url) {
        Imager.loadDefer(this, url, new SimpleTarget<Bitmap>() {

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                imageView.setImageResource(R.drawable.error_holder);
                UiUtils.showSnackLong(imageView, R.string.picture_load_fail, R.string.retry,
                        v -> load(url));
            }

            @Override
            public void onResourceReady(Bitmap b, GlideAnimation<? super Bitmap> arg1) {
                imageView.setImageBitmap(b);
                startTransition();
                bitmap = b;
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
                    Share.shareImage(context, uri);
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


    @Override
    public void onPhotoTap(View view, float x, float y) {
        getActivity().onBackPressed();
    }

    @Override
    public void onOutsidePhotoTap() {
        getActivity().onBackPressed();
    }
}
