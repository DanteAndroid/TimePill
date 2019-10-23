package com.dante.diary.edit;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.blankj.utilcode.utils.KeyboardUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dante.diary.R;
import com.dante.diary.base.BaseActivity;
import com.dante.diary.base.Constants;
import com.dante.diary.custom.PickPictureActivity;
import com.dante.diary.login.LoginManager;
import com.dante.diary.model.Notebook;
import com.dante.diary.net.HttpErrorAction;
import com.dante.diary.net.NetService;
import com.dante.diary.utils.DateUtil;
import com.dante.diary.utils.UiUtils;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import butterknife.BindView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.dante.diary.custom.PickPictureActivity.REQUEST_PICK_PICTURE;

public class EditNotebookActivity extends BaseActivity {
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
    //    @BindView(R.id.expireCalendar)
    CalendarView expireCalendar;
    @BindView(R.id.privacy)
    Switch privacy;

    String notebookSubject;
    String description;
    boolean isPrivate;
    int notebookId;
    @BindView(R.id.notebookCover)
    ImageView notebookCover;
    @BindView(R.id.month)
    RadioButton month;
    @BindView(R.id.halfYear)
    RadioButton halfYear;
    @BindView(R.id.aYear)
    RadioButton aYear;
    @BindView(R.id.custom)
    RadioButton custom;
    @BindView(R.id.expireTimeGroup)
    RadioGroup expireTimeGroup;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appBar)
    AppBarLayout appBar;
    @BindView(R.id.visibility)
    TextView visibility;
    @BindView(R.id.noteBookExpireTime)
    TextView noteBookExpireTime;
    //    @BindView(R.id.calendarScrollView)
//    ScrollView calendarScrollView;
    private boolean isEditMode;
    private Notebook notebook;
    private File coverFile;
    private String expireDate;
    private boolean notebookChanged;
    private boolean coverChanged;
    private DatePickerDialog pickerDialog;

    @Override
    protected int initLayoutId() {
        return R.layout.activity_create_notebook;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        supportPostponeEnterTransition();
        super.initViews(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new Slide(Gravity.RIGHT));
        }

        if (getIntent().getExtras() != null) {
            notebookId = getIntent().getIntExtra(Constants.ID, 0);
            notebook = getBase().findNotebook(notebookId);
            if (notebook == null) {
                UiUtils.showSnack(notebookCover, R.string.unable_to_find_notebook);
                return;
            }
            isEditMode = notebookId > 0;
            getToolbar().setTitle(R.string.edit_notebook);
        }

        initCover();
        initTextInput();
        initCalendar();

        privacy.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isPrivate = !isChecked;
            notifyNotebookChanged();
        });
    }

    private void initExpireTimeGroup() {

        custom.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showPicker();
            }
        });
        expireTimeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.month:
                    expireDate = DateUtil.getDisplayDay(DateUtil.nextMonthDateOfToday());
                    break;
                case R.id.halfYear:
                    expireDate = DateUtil.getDisplayDay(DateUtil.nextMonthsOfToday(6));
                    break;
                case R.id.aYear:
                    expireDate = DateUtil.getDisplayDay(DateUtil.nextMonthsOfToday(12));
                    break;
                case R.id.custom:
                    showPicker();
                    break;
                default:
                    expireDate = DateUtil.getDisplayDay(DateUtil.nextMonthDateOfToday());
                    break;
            }
        });
    }

    private void showPicker() {
        if (pickerDialog == null) {
            Date date = DateUtil.nextMonthDateOfToday();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            pickerDialog = new DatePickerDialog(
                    EditNotebookActivity.this, (view, year, month, dayOfMonth) -> {
                expireDate = year + "-" + (++month) + "-" + dayOfMonth;
                custom.setText(expireDate);
            },
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            pickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), (dialog, which) -> {
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    expireTimeGroup.check(R.id.month);
                    custom.setText(R.string.custom);
                }
            });
        }
        pickerDialog.show();
    }

    private void initCover() {
        notebookCover.setOnClickListener(v -> startActivityForResult(new Intent(getApplicationContext(), PickPictureActivity.class), REQUEST_PICK_PICTURE));

        if (isEditMode) {
            ViewCompat.setTransitionName(notebookCover, String.valueOf(notebookId));
            Glide.with(this)
                    .asBitmap()
                    .load(notebook.getCoverUrl())
                    .error(R.drawable.portrait_holder)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            notebookCover.setImageBitmap(resource);
                            supportStartPostponedEnterTransition();
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            supportStartPostponedEnterTransition();
                        }
                    });
        } else {
            Glide.with(this)
                    .load(R.drawable.default_cover)
                    .into(notebookCover);
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
                .subscribe(notebook1 -> {
                            coverChanged = true;
                            notifyNotebookChanged();
                            UiUtils.showSnack(notebookCover, getString(R.string.cover_upload_success));
                        },
                        throwable -> UiUtils.showSnack(notebookCover, getString(R.string.cover_upload_failed)));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_PICTURE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                String path = data.getStringExtra("path");
                notebookCover.setImageURI(uri);
                coverFile = new File(path);
                setNoteBookCover();
            } else if (resultCode == PickPictureActivity.RESULT_FAILED) {
                UiUtils.showSnack(notebookCover, getString(R.string.fail_read_pictures));
            }

        }
    }

    private void initCalendar() {
        expireDate = DateUtil.getDisplayDay(DateUtil.nextMonthDateOfToday());
        if (isEditMode) {
            expireTimeGroup.setVisibility(View.GONE);
            noteBookExpireTime.setVisibility(View.VISIBLE);
            noteBookExpireTime.setText(notebook.getExpired());
        } else {
            initExpireTimeGroup();
//            expireCalendar.setMinDate(DateUtil.nextMonthDateOfToday().getTime());
//            expireCalendar.setDate(DateUtil.nextMonthDateOfToday().getTime());
//            expireCalendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
//                expireDate = year + "-" + (++month) + "-" + dayOfMonth;
//                expire.setText(String.format(getString(R.string.expire_time), expireDate));
//            });
        }
    }

    private void initTextInput() {
        if (isEditMode) {
            notebookSubject = notebook.getSubject();
            subject.setText(notebookSubject);
            desc.setText(notebook.getDescription());
            privacy.setChecked(notebook.isIsPublic());
            subject.setSelection(subject.getText().length());
            desc.append("");
        }

        new Handler().postDelayed(() -> KeyboardUtils.showSoftInput(subject), 600);
        subject.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                notifyNotebookChanged();
                String result = s.toString().trim();
                notebookSubject = result;
                if (result.length() <= 0) {
                    subjectWrapper.setError(getString(R.string.subject_cant_be_empty));
                } else if (result.length() > 20) {
                    subjectWrapper.setError(getString(R.string.subject_is_long));
                } else {
                    subjectWrapper.setError(null);
                }
            }
        });
        desc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                notifyNotebookChanged();
                description = s.toString().trim();
            }
        });
    }

    private void notifyNotebookChanged() {
        notebookChanged = true;
        invalidateOptionsMenu();
    }


    public void send() {
        if (isEditMode && !notebookChanged) {
            supportFinishAfterTransition();
            return;
        }

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
                        UiUtils.showSnack(notebookCover, R.string.update_success);
                    } else {
                        notebook = n;
                        notebookId = n.getId();
                        String s = String.format(getString(R.string.create_notebook_success), notebookSubject);
                        UiUtils.showSnack(notebookCover, s);
                    }
                    setResult(RESULT_OK);
                    new Handler().postDelayed(() -> {
                        if (coverChanged) {
                            finish();
                        } else {
                            supportFinishAfterTransition();
                        }
                    }, 1000);
                }, new HttpErrorAction<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        super.call(throwable);
                        if (!TextUtils.isEmpty(errorMessage)) {
                            if (isEditMode) {
                                UiUtils.showSnack(notebookCover, String.format(getString(R.string.update_failed) + " ", errorMessage));
                            } else {
                                UiUtils.showSnack(notebookCover, getString(R.string.fail_to_create_notebook) + " " + errorMessage);
                            }
                        }
                    }
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean enabled = !TextUtils.isEmpty(notebookSubject) || notebookChanged;
        MenuItem item = menu.findItem(R.id.action_send);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        Drawable resIcon = ContextCompat.getDrawable(this, R.drawable.ic_send_white_36px);
        if (!enabled) {
            resIcon.mutate().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        }
        item.setEnabled(enabled);
        item.setIcon(resIcon);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_send) {
            send();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_send, menu);
        return true;
    }


}
