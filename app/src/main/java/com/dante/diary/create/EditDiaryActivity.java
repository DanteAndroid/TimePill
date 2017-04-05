package com.dante.diary.create;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.blankj.utilcode.utils.KeyboardUtils;
import com.bumptech.glide.Glide;
import com.dante.diary.R;
import com.dante.diary.base.BaseActivity;
import com.dante.diary.base.Constants;
import com.dante.diary.custom.PickPictureActivity;
import com.dante.diary.login.LoginManager;
import com.dante.diary.model.Diary;
import com.dante.diary.model.Notebook;
import com.dante.diary.net.NetService;
import com.dante.diary.utils.Imager;
import com.dante.diary.utils.SpUtil;
import com.dante.diary.utils.UiUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import rx.Observable;

import static com.dante.diary.custom.PickPictureActivity.REQUEST_PICK_PICTURE;

public class EditDiaryActivity extends BaseActivity {
    public static final int DIARY_CONTENT_TEXT_LIMIT = 10;
    private static final String TAG = "CreateDiaryActivity";
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
    @BindView(R.id.contentWrapper)
    TextInputLayout contentWrapper;
    @BindView(R.id.root)
    LinearLayout root;
    @BindView(R.id.attachPhoto)
    ImageView attachPhoto;

    private String diaryContent;
    private int notebookId;
    private int diaryId;
    private List<Notebook> notebooks;
    private File photoFile;
    private boolean isEditMode;
    private Diary diary;


    @Override
    protected int initLayoutId() {
        return R.layout.activity_create_diary;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);

        if (getIntent().getExtras() != null) {
            diaryId = getIntent().getIntExtra(Constants.ID, 0);
            diary = base.findDiary(diaryId);
            if (diary == null) {
                fetchDiary();
            } else {
                inflateDiary();
            }
            isEditMode = diaryId > 0;
            toolbar.setTitle(R.string.edit_diary);
        }
        fetchSubjects();
        initEditText();
        initTools();
    }

    private void initTools() {
        if (isEditMode && TextUtils.isEmpty(diary.getPhotoThumbUrl())) {
            photo.setVisibility(View.GONE);
        }
        photo.setOnClickListener(v -> startActivityForResult(new Intent(getApplicationContext(), PickPictureActivity.class), REQUEST_PICK_PICTURE));

        palette.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_PICTURE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                String path = data.getStringExtra("path");
                retrievePicture(uri, path);

            } else if (resultCode == PickPictureActivity.RESULT_FAILED) {
                UiUtils.showSnack(photo, getString(R.string.fail_read_pictures));
            }

        }
    }

    private void retrievePicture(Uri uri, String path) {
        attachPhoto.setVisibility(View.VISIBLE);
        Glide.with(this).load(uri).into(attachPhoto);
        photoFile = new File(path);
    }

    private void fetchDiary() {
        LoginManager.getApi().getDiaryDetail(diaryId)
                .compose(applySchedulers())
                .subscribe(diary1 -> {
                    EditDiaryActivity.this.diary = diary1;
                    inflateDiary();
                }, throwable -> {
                    UiUtils.showSnackLong(content, R.string.cant_get_diary);
                    throwable.printStackTrace();
                });
    }

    private void inflateDiary() {
        if (diary != null) {
            notebookId = diary.getNotebookId();
            content.append(diary.getContent());
            attachPhoto.setVisibility(View.VISIBLE);
            Imager.load(this, diary.getPhotoThumbUrl(), attachPhoto);
        }
    }

    private void initEditText() {
        String draft = SpUtil.getString("draft");
        if (!draft.isEmpty()) {
            content.append(draft);
            SpUtil.remove("draft");
            UiUtils.showSnack(content, getString(R.string.draft_restored));
        }
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
                if (diaryContent.length() <= DIARY_CONTENT_TEXT_LIMIT) {
                    contentWrapper.setError("再说点什么吧");
                } else {
                    contentWrapper.setError("");
                    SpUtil.save("draft", diaryContent);
                }
                invalidateOptionsMenu();
            }
        });
    }


    private void fetchSubjects() {
        LoginManager.getApi()
                .getMyNotebooks(LoginManager.getMyId())
                .compose(applySchedulers())
                .subscribe(notebooks -> {
                    this.notebooks = notebooks;
                    base.save(notebooks);
                    List<String> subjects = new ArrayList<>();
                    for (Notebook n : notebooks) {
                        if (!n.isExpired()) {
                            subjects.add(n.getSubject());
                        }
                    }
                    if (subjects.isEmpty()) {
                        KeyboardUtils.hideSoftInput(this);
                        UiUtils.showSnackLong(subjectSpinner, getString(R.string.no_valid_notebook));
                    }
                    initSubjectSpinner(subjects);

                }, throwable -> UiUtils.showSnackLong(subjectSpinner, getString(R.string.unable_to_fetch_notebooks), R.string.create_notebook, v -> {
                    startActivity(new Intent(getApplicationContext(), EditNotebookActivity.class));
                    finish();
                }));

    }

    private void send() {
        source()
                .compose(applySchedulers())
                .subscribe(notebook -> {
                    Log.d(TAG, "call: " + notebook.getContent());
                    UiUtils.showSnack(subjectSpinner, isEditMode ? R.string.diary_update_success : R.string.create_diary_success);

                    setResult(RESULT_OK);
                    supportFinishAfterTransition();

                }, throwable -> {

                    KeyboardUtils.hideSoftInput(EditDiaryActivity.this);
                    UiUtils.showSnackLong(subjectSpinner, R.string.create_diary_failed);
                    throwable.printStackTrace();
                });
    }

    private Observable<Diary> source() {
        if (isEditMode) {
            return LoginManager.getApi().updateDiary(diaryId, diaryContent, notebookId);
        }

        return LoginManager.getApi()
                .createDiary(notebookId, NetService.getRequestBody(diaryContent),
                        photoFile == null ? null : NetService.createMultiPart("photo", photoFile));
    }

    private void initSubjectSpinner(List<String> list) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(R.layout.spinner_subject_dropdown_item);
        subjectSpinner.setAdapter(adapter);
        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                notebookId = notebooks.get(position).getId();
                invalidateOptionsMenu();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                UiUtils.showSnack(subjectSpinner, getString(R.string.choose_notebook_hint));
            }
        });
        subjectSpinner.animate().alpha(1).setStartDelay(300).start();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean enabled;
        if (isEditMode && diaryContent != null && diary != null) {
            enabled = (!diaryContent.equals(diary.getContent()) && diaryContent.length() > DIARY_CONTENT_TEXT_LIMIT) || notebookId != diary.getNotebookId();
        } else {
            enabled = !TextUtils.isEmpty(diaryContent)
                    && diaryContent.length() > DIARY_CONTENT_TEXT_LIMIT;
        }
        if (photoFile != null && photoFile.exists()) {
            enabled = true;
        }
        MenuItem item = menu.findItem(R.id.action_send);
        Drawable resIcon = getResources().getDrawable(R.drawable.ic_send_white_36px, getTheme());
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
