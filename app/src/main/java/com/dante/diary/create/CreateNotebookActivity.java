package com.dante.diary.create;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dante.diary.R;
import com.dante.diary.base.BaseActivity;
import com.dante.diary.base.Constants;
import com.dante.diary.custom.PickPictureActivity;
import com.dante.diary.login.LoginManager;
import com.dante.diary.model.Notebook;
import com.dante.diary.net.NetService;
import com.dante.diary.utils.BitmapUtil;
import com.dante.diary.utils.DateUtil;
import com.dante.diary.utils.UiUtils;

import java.io.File;
import java.util.HashMap;

import butterknife.BindView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CreateNotebookActivity extends BaseActivity implements View.OnClickListener {
    public static final int PRIVACY_PRIVATE = 1;
    public static final int PRIVACY_PUBLIC = 10;
    private static final String TAG = "CreateNotebookActivity";
    @BindView(R.id.subject)
    TextInputEditText subject;
    @BindView(R.id.subjectWrapper)
    TextInputLayout subjectWrapper;
    @BindView(R.id.desc)
    TextInputEditText desc;
    @BindView(R.id.descWrapper)
    TextInputLayout descWrapper;
    @BindView(R.id.expire)
    TextView expire;
    @BindView(R.id.expireCalendar)
    CalendarView expireCalendar;
    @BindView(R.id.privacy)
    Switch privacy;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    String notebookSubject;
    String description;
    boolean isPrivate;
    int notebookId;
    @BindView(R.id.notebookCover)
    ImageView notebookCover;
//    @BindView(R.id.calendarScrollView)
    ScrollView calendarScrollView;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout toolbarLayout;
    private boolean isEditMode;
    private Notebook notebook;
    private File coverFile;
    private String expireDate;

    @Override
    protected int initLayoutId() {
        return R.layout.activity_create_notebook;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        supportPostponeEnterTransition();
        super.initViews(savedInstanceState);
//        getWindow().setEnterTransition(null);
        if (getIntent() != null) {
            notebookId = getIntent().getIntExtra(Constants.ID, 0);
            notebook = base.findNotebook(notebookId);
            isEditMode = notebookId > 0;
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        String title = getString(isEditMode ? R.string.edit_notebook : R.string.create_notebook);
        toolbarLayout.setTitle(title);

        initCover();
        initTextInput();
        initCalendar();

        privacy.setOnCheckedChangeListener((buttonView, isChecked) -> isPrivate = !isChecked);
        fab.setOnClickListener(this);
    }

    private void initCover() {
        notebookCover.setOnClickListener(v -> startActivityForResult(new Intent(getApplicationContext(), PickPictureActivity.class), 1));

        if (isEditMode) {
            ViewCompat.setTransitionName(notebookCover, String.valueOf(notebookId));
            Log.d(TAG, "initCover: " + notebookId);
            Glide.with(this)
                    .load(notebook.getCoverUrl())
                    .asBitmap()
                    .error(R.drawable.portrait_holder)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            supportStartPostponedEnterTransition();
                        }

                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            notebookCover.setImageBitmap(resource);
                            supportStartPostponedEnterTransition();
                        }
                    });
        } else {

        }

    }

    private void setNoteBookCover() {
        if (notebookId <= 0) {
            return;
        }
        if (coverFile == null || !coverFile.exists()) {
            return;
        }

        LoginManager.getApi().setNotebookCover(notebookId, NetService.createMultiPart("cover", coverFile))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(notebook1 -> UiUtils.showSnack(notebookCover, getString(R.string.cover_upload_success)),
                        throwable -> UiUtils.showSnack(notebookCover, getString(R.string.cover_upload_failed)));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {

            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                notebookCover.setImageURI(uri);
                String path = BitmapUtil.getPath(uri);
                if (path == null) {
                    path = data.getStringExtra("path");
                }
                coverFile = new File(path);
                setNoteBookCover();

            } else if (resultCode == PickPictureActivity.RESULT_FAILED) {
                UiUtils.showSnack(notebookCover, getString(R.string.fail_read_pictures));
            } else if (resultCode == RESULT_CANCELED) {
                UiUtils.showSnack(notebookCover, getString(R.string.action_canceled));
            }

        }
    }

    private void initCalendar() {
        expireDate = DateUtil.getDisplayDay(DateUtil.nextMonthDateOfToday());
        expire.setText(String.format(getString(R.string.expire_time),
                isEditMode ? notebook.getExpired() : expireDate));
        if (isEditMode) {
            calendarScrollView.setVisibility(View.GONE);
        } else {
            expireCalendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                expireDate = year + "-" + month + "-" + dayOfMonth;
                expire.setText(String.format(getString(R.string.expire_time), expireDate));
            });

            expireCalendar.setDate(DateUtil.nextMonthDateOfToday().getTime(), true, true);
        }
    }

    private void initTextInput() {
        if (notebook != null) {
            subject.setText(notebook.getSubject());
        }

        subject.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String result=s.toString().replace(" ", "");
                if (result.length() <= 0) {
                    subjectWrapper.setError("标题不能为空");
                } else if (result.length()>20){
                    subjectWrapper.setError("标题有点长哦");
                } else{
                    subjectWrapper.setError(null);
                    notebookSubject = s.toString();
                    fab.show();
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        HashMap<String, Object> data = new HashMap<>();
        data.put(Constants.SUBJECT, notebookSubject);
        if (!TextUtils.isEmpty(description)) {
            data.put(Constants.DESCRIPTION, description);
        }
        data.put(Constants.PRIVACY, isPrivate ? PRIVACY_PRIVATE : PRIVACY_PUBLIC);
        data.put(Constants.EXPIRED, expireDate);

        source(data).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(n -> {
                    if (isEditMode) {
                        UiUtils.showSnack(fab, R.string.update_success);
                    } else {
                        notebook = n;
                        notebookId = n.getId();
                        setNoteBookCover();
                        String s = String.format(getString(R.string.create_notebook_success), notebookSubject);
                        UiUtils.showSnack(fab, s);
                    }

                    supportFinishAfterTransition();

                }, throwable -> {
                    if (isEditMode) {
                        UiUtils.showSnack(fab, String.format(getString(R.string.update_failed) + " %s", throwable.getMessage()));
                    } else {
                        UiUtils.showSnack(fab, getString(R.string.fail_to_create_notebook));
                    }
                    throwable.printStackTrace();
                });
    }


    public Observable<Notebook> source(HashMap<String, Object> data) {
        if (isEditMode) {
            return LoginManager.getApi()
                    .updateNotebook(notebookId, data);
        }
        return LoginManager.getApi()
                .createNotebook(data);
    }
}
