package com.dante.diary.edit;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;

import com.blankj.utilcode.utils.KeyboardUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.dante.diary.R;
import com.dante.diary.base.BaseActivity;
import com.dante.diary.base.Constants;
import com.dante.diary.custom.PickPictureActivity;
import com.dante.diary.draw.DrawActivity;
import com.dante.diary.login.LoginManager;
import com.dante.diary.main.MainActivity;
import com.dante.diary.model.Diary;
import com.dante.diary.model.Notebook;
import com.dante.diary.net.HttpErrorAction;
import com.dante.diary.net.NetService;
import com.dante.diary.utils.ImageProgresser;
import com.dante.diary.utils.Imager;
import com.dante.diary.utils.SpUtil;
import com.dante.diary.utils.UiUtils;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.dante.diary.custom.PickPictureActivity.REQUEST_PICK_PICTURE;

public class EditDiaryActivity extends BaseActivity {
    public static final int DIARY_CONTENT_TEXT_LIMIT = 5;
    private static final String TAG = "EditDiaryActivity";
    private static final int REQUEST_DRAW = 2;
    @BindView(R.id.subjectSpinner)
    Spinner subjectSpinner;
    @BindView(R.id.content)
    EditText content;
    @BindView(R.id.photo)
    ImageView photo;
    @BindView(R.id.palette)
    ImageView palette;
    @BindView(R.id.emoji)
    ImageView emoji;
    //    @BindView(R.id.contentWrapper)
//    TextInputLayout contentWrapper;
    @BindView(R.id.root)
    LinearLayout root;
    @BindView(R.id.attachPhoto)
    ImageView attachPhoto;
    @BindView(R.id.useTopicPic)
    Switch useTopicPic;

    private String diaryContent;
    private int notebookId;
    private int diaryId;
    private File photoFile;
    private boolean isEditMode;
    private Diary diary;
    private ArrayList<Notebook> validSubjects = new ArrayList<>();
    private boolean isTopic;
    private File topicPictureFile;
    private String sharedText;
    private Uri sharedImageUri;


    @Override
    protected int initLayoutId() {
        return R.layout.activity_create_diary;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        if (getIntent().getExtras() != null) {
            diaryId = getIntent().getIntExtra(Constants.ID, 0);
            isTopic = getIntent().getBooleanExtra("isTopic", false);

            if (isTopic) {
                getToolbar().setTitle(R.string.edit_topic_diary);
            }
            isEditMode = diaryId > 0;
            if (isEditMode) {
                diary = getBase().findDiary(diaryId);
                if (diary == null) {
                    fetchDiary();
                } else {
                    inflateDiary();
                }
                getToolbar().setTitle(R.string.edit_diary);
            }
            handleSendAction();
        }
        fetchTopicPicture();
        fetchSubjects();
        initEditText();
        initTools();
    }

    private void handleSendAction() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            } else if (type.startsWith("image/")) {
                sharedImageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            }
        } else {
            // Handle other intents, such as being started from the home screen
        }
    }

    private void fetchTopicPicture() {
        if (isTopic) {
            useTopicPic.setVisibility(View.VISIBLE);
            useTopicPic.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    photoFile = topicPictureFile;
                } else {
                    photoFile = null;
                }
            });
            new Thread(() -> {
                try {
                    topicPictureFile = Glide.with(EditDiaryActivity.this).load(SpUtil.getString(Constants.TOPIC_PICTURE))
                            .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
                    photoFile = topicPictureFile;
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void initTools() {
        if (isEditMode) {
            photo.setVisibility(View.GONE);
            palette.setVisibility(View.GONE);
            useTopicPic.setVisibility(View.GONE);
        }
        photo.setOnClickListener(v -> startActivityForResult(new Intent(getApplicationContext(), PickPictureActivity.class), REQUEST_PICK_PICTURE));
        palette.setOnClickListener(v -> {
            KeyboardUtils.hideSoftInput(EditDiaryActivity.this);
            startActivityForResult(new Intent(getApplicationContext(), DrawActivity.class), REQUEST_DRAW);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_PICTURE) {
            if (resultCode == RESULT_OK) {
                String path = data.getStringExtra("path");
                retrievePicture(null, path);
            } else if (resultCode == PickPictureActivity.RESULT_FAILED) {
                UiUtils.showSnack(photo, getString(R.string.fail_read_pictures));
            }

        } else if (requestCode == REQUEST_DRAW) {
            if (resultCode == RESULT_OK) {
                String path = data.getStringExtra("path");
                retrievePicture(null, path);
            }
        }
    }

    private void retrievePicture(Uri uri, String path) {
        attachPhoto.setVisibility(View.VISIBLE);
        attachPhoto.setOnClickListener(v -> new AlertDialog.Builder(EditDiaryActivity.this)
                .setMessage(R.string.delete_photo_hint)
                .setPositiveButton(R.string.remove, (dialog, which) -> {
                    attachPhoto.setVisibility(View.GONE);
                    photoFile = null;
                }).show());
        if (uri == null) {
            photoFile = new File(path);
        } else {
            RxPermissions permissions = new RxPermissions(this);
            permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(grant -> {
                        if (grant) {
                            photoFile = saveFile(uri);
                        } else {
                            UiUtils.showSnack(getWindow().getDecorView(), getString(R.string.unable_upload_picture));
                        }
                    });
        }
        Glide.with(this).load(photoFile).diskCacheStrategy(DiskCacheStrategy.NONE).into(attachPhoto);
    }

    private File saveFile(Uri uri) {
        File photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "temp" + ".jpg");
        if (photo.exists()) {
            photo.delete();
        }
        try {
            boolean exist = photo.createNewFile();
            if (!exist) {
                Log.e(TAG, "saveToFile: can't create file");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            if (bitmap != null) {
                OutputStream os = new BufferedOutputStream(new FileOutputStream(photo));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                Log.i(TAG, "saveToFile: scale bitmap " + bitmap.getWidth() + ", " + bitmap.getHeight());
                os.close();
            }

        } catch (IOException e) {
            Log.e(TAG, "saveToFile: write temp file failed");
            e.printStackTrace();
        }
        return photo;
    }

    private void fetchDiary() {
        LoginManager.getApi().getDiaryDetail(diaryId)
                .compose(applySchedulers())
                .subscribe(d -> {
                    EditDiaryActivity.this.diary = d;
                    inflateDiary();
                }, throwable -> {
                    UiUtils.showSnackLong(content, getString(R.string.cant_get_diary) + throwable.getMessage());
                });
    }

    private void inflateDiary() {
        if (diary != null) {
            notebookId = diary.getNotebookId();
            content.setText(diary.getContent());
            content.setSelection(content.getText().length());
            if (!TextUtils.isEmpty(diary.getPhotoThumbUrl())) {
                attachPhoto.setVisibility(View.VISIBLE);
                Imager.load(this, diary.getPhotoThumbUrl(), attachPhoto);
            }
        }
    }

    private void initEditText() {
        if (TextUtils.isEmpty(sharedText)) {
            diaryContent = SpUtil.getString("draft");
            if (!diaryContent.isEmpty()) {
                content.setText(diaryContent);
                content.setSelection(content.getText().length());
                SpUtil.remove("draft");
                UiUtils.showSnack(content, getString(R.string.draft_restored));
            }
        } else {
            diaryContent = sharedText;
            content.setText(diaryContent);
            content.setSelection(content.getText().length());
        }
        String[] hints = getResources().getStringArray(R.array.create_diary_hints);
        content.setHint(hints[new Random().nextInt(hints.length)]);
        content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                diaryContent = s.toString().trim();
                if (diaryContent.length() < DIARY_CONTENT_TEXT_LIMIT) {
//                    contentWrapper.setError(getString(R.string.say_more));
                } else {
                    SpUtil.save("draft", diaryContent);
                    if (!SpUtil.getBoolean("type_hint")) {
                        SpUtil.save("type_hint", true);
                        UiUtils.showSnack(content, R.string.type_hint);
                    }
                }
                invalidateOptionsMenu();
            }
        });
        invalidateOptionsMenu();
        new Handler().postDelayed(() -> KeyboardUtils.showSoftInput(content), 300);
    }


    private void fetchSubjects() {
        LoginManager.getApi()
                .getMyNotebooks(LoginManager.getMyId())
                .compose(applySchedulers())
                .subscribe(notebooks -> {
                    getBase().save(notebooks);
                    checkValidNotebooks(notebooks);
                    initSubjectSpinner(validSubjects);
                }, throwable -> UiUtils.showSnackLong(subjectSpinner, getString(R.string.unable_to_fetch_notebooks), R.string.create_notebook, v -> {
                    startActivity(new Intent(getApplicationContext(), EditNotebookActivity.class));
                    finish();
                }));

    }

    private void checkValidNotebooks(List<Notebook> notebooks) {
        for (Notebook n : notebooks) {
            if (!n.isExpired()) {
                validSubjects.add(n);
            }
        }
        if (validSubjects.isEmpty()) {
            KeyboardUtils.hideSoftInput(this);
            UiUtils.showSnackLong(subjectSpinner, getString(R.string.no_valid_notebook), R.string.create_notebook, v -> {
                startActivity(new Intent(EditDiaryActivity.this, EditNotebookActivity.class));
                finish();
            });
        }
    }

    private void send() {
        if (diaryContent == null || diaryContent.length() < DIARY_CONTENT_TEXT_LIMIT) {
            UiUtils.showSnack(content, R.string.say_more);
            return;
        }

        ProgressBar progressBar = ImageProgresser.attachProgress(content);
        source().compose(applySchedulers())
                .subscribe(notebook -> {
                    UiUtils.showSnack(subjectSpinner, isEditMode ? R.string.diary_update_success : R.string.create_diary_success);
                    SpUtil.remove("draft");
                    setResult(RESULT_OK);
                    supportFinishAfterTransition();
                    if (sharedImageUri != null || sharedText != null) {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }

                }, new HttpErrorAction<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        super.call(throwable);
                        progressBar.setVisibility(View.GONE);
                        KeyboardUtils.hideSoftInput(EditDiaryActivity.this);
                        if (!TextUtils.isEmpty(errorMessage)) {
                            UiUtils.showSnackLong(subjectSpinner, getString(R.string.create_diary_failed) + " " + errorMessage);
                        }
                    }
                });
    }

    private Observable<Diary> source() {
        if (isEditMode) {
            return LoginManager.getApi().updateDiary(diaryId, diaryContent, notebookId);
        }
        return LoginManager.getApi()
                .createDiary(notebookId, NetService.getRequestBody(diaryContent), isTopic ? NetService.getRequestBody("1") : null,
                        photoFile == null ? null : NetService.createMultiPart("photo", photoFile));
    }


    private void initSubjectSpinner(List<Notebook> validNotebooks) {
        List<String> subjects = new ArrayList<>();
        for (Notebook s : validNotebooks) {
            subjects.add(s.getSubject());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_layout, subjects);
        adapter.setDropDownViewResource(R.layout.spinner_subject_dropdown_item);
        subjectSpinner.setAdapter(adapter);
        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                notebookId = validNotebooks.get(position).getId();
                invalidateOptionsMenu();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                UiUtils.showSnack(subjectSpinner, getString(R.string.choose_notebook_hint));
            }
        });
        subjectSpinner.animate().alpha(1).start();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean enabled;
        if (isEditMode && diaryContent != null && diary != null) {
            enabled = (!diaryContent.equals(diary.getContent())) || notebookId != diary.getNotebookId();
        } else {

            enabled = !TextUtils.isEmpty(diaryContent);
        }
        if (photoFile != null && photoFile.exists()) {
            enabled = true;
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        if (sharedImageUri != null || sharedText != null) {
//            startActivity(new Intent(getApplicationContext(), MainActivity.class));
//        }
    }
}
