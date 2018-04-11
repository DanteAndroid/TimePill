package com.dante.diary.detail;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.utils.ClipboardUtils;
import com.blankj.utilcode.utils.IntentUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.dante.diary.R;
import com.dante.diary.base.BaseFragment;
import com.dante.diary.base.Constants;
import com.dante.diary.custom.BottomDialogFragment;
import com.dante.diary.edit.EditDiaryActivity;
import com.dante.diary.interfaces.IOnItemClickListener;
import com.dante.diary.login.LoginManager;
import com.dante.diary.model.Comment;
import com.dante.diary.model.Diary;
import com.dante.diary.net.HttpErrorAction;
import com.dante.diary.net.TimeApi;
import com.dante.diary.utils.DateUtil;
import com.dante.diary.utils.ImageProgresser;
import com.dante.diary.utils.Share;
import com.dante.diary.utils.SpUtil;
import com.dante.diary.utils.TextChecker;
import com.dante.diary.utils.TransitionHelper;
import com.dante.diary.utils.UiUtils;
import com.dante.diary.utils.WrapContentLinearLayoutManager;
import com.jaychang.st.SimpleText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static com.dante.diary.R.id.commit;

/**
 * A simple {@link Fragment} subclass.
 */
public class DiaryDetailFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, IOnItemClickListener, View.OnLongClickListener {

    public static final String COMMENT_ID = "commentId";
    private static final String TAG = "DiaryDetailFragment";
    DiaryCommentsAdapter adapter;
    WrapContentLinearLayoutManager layoutManager;
    @BindView(R.id.diaryDate)
    TextView diaryDate;
    @BindView(R.id.myAvatar)
    ImageView myAvatar;
    @BindView(R.id.time)
    TextView time;
    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.attachPicture)
    ImageView attachPicture;
    @BindView(R.id.commentsCount)
    TextView commentsCount;
    @BindView(R.id.commentsList)
    RecyclerView commentsRecycler;
    @BindView(R.id.myName)
    TextView myName;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    Diary diary;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.noComment)
    TextView noComment;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.divider)
    View divider;
    @BindView(R.id.diary_layout)
    RelativeLayout diaryLayout;
    @BindView(R.id.scrollView)
    NestedScrollView scrollView;
    @BindView(R.id.isGif)
    TextView isGif;
    private ShareActionProvider mShareActionProvider;
    private int diaryId;
    private String commentTemp;
    private BottomDialogFragment commentFragment;
    private long start;

    public DiaryDetailFragment() {
        // Required empty public constructor
    }

    public static DiaryDetailFragment newInstance(int diaryId) {

        Bundle args = new Bundle();
        args.putInt(Constants.ID, diaryId);
        DiaryDetailFragment fragment = new DiaryDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static DiaryDetailFragment newInstance(int diaryId, int commentId) {
        Bundle args = new Bundle();
        args.putInt(Constants.ID, diaryId);
        args.putInt(COMMENT_ID, commentId);
        DiaryDetailFragment fragment = new DiaryDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int initLayoutId() {
        return R.layout.fragment_diary_detail;
    }

    @Override
    protected void setAnimations() {

    }

    @Override
    protected void initViews() {
        setHasOptionsMenu(true);//填充menu（执行onCreateOptionsMenu）
        if (getArguments() != null) {  //有参数则获取参数id
            diaryId = getArguments().getInt(Constants.ID);
        }
        adapter = new DiaryCommentsAdapter(null, this);
        layoutManager = new WrapContentLinearLayoutManager(getContext());
        commentsRecycler.setLayoutManager(layoutManager);
        commentsRecycler.setNestedScrollingEnabled(false);//recyclerView在NestedScrollView中顺滑的秘诀
        commentsRecycler.setAdapter(adapter);
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorPrimary),
                ContextCompat.getColor(getContext(), R.color.colorAccent), ContextCompat.getColor(getContext(), R.color.indigo_500));
        swipeRefresh.setOnRefreshListener(this);
        commentsRecycler.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onItemLongClick(BaseQuickAdapter quickAdapter, View view, int position) {
                Comment comment = adapter.getItem(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                boolean isMyComment = comment.getUserId() == SpUtil.getInt(Constants.ID);
                String[] items = getResources().getStringArray(R.array.comment_menu);
                if (isMyComment) {
                    items = getResources().getStringArray(R.array.comment_menu_my);
                }
                builder.setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        ClipboardUtils.copyText(comment.getContent());
                        UiUtils.showSnack(rootView, getString(R.string.copyed));
                    } else if (which == 1) {
                        Share.shareText(getActivity(), Share.appendUrl(comment.getContent()));
                    } else if (which == 2) {
                        reportComment(comment.getId(), position);
                    } else if (which == 3) {
                        deleteComment(comment.getId(), position);
                    }

                }).show();
            }

            @Override
            public void onItemChildClick(BaseQuickAdapter quickAdapter, View view, int position) {
                int id = view.getId();
                Comment comment = adapter.getItem(position);
                if (id == R.id.commentAvatar || id == R.id.commentName) {
                    goProfile(comment.getUserId());
                } else if (id == R.id.commentContent) {
                    comment(comment.getUserId(), comment.getUser().getName());
                }
            }

            @Override
            public void onSimpleItemClick(BaseQuickAdapter quickAdapter, View view, int position) {
                Comment comment = adapter.getItem(position);
                comment(comment.getUserId(), comment.getUser().getName());
            }
        });
        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                int dy = scrollY - oldScrollY;
                if (dy > 5) {
                    fab.hide();
                } else if (dy < -5) {
                    fab.show();
                }
            }
        });
    }

    private void reportComment(int id, int position) {
        UiUtils.showSnack(rootView, "暂时不支持举报回复功能，如果您有任何建议欢迎到APP设置里反馈~");
    }

    private void deleteComment(int commentId, int position) {
        LoginManager.getApi().deleteComment(commentId)
                .compose(applySchedulers())
                .subscribe(responseBodyResponse -> {
                    adapter.remove(position);
                }, throwable -> {
                    UiUtils.showSnack(rootView, R.string.delete_comment_failed + throwable.getMessage());
                });
    }


    @Override
    protected void initData() {
        setToolbarScrollFlag(false);
        //本来 onCreateOptionsMenu 已填充了分享Menu，但是却遇到了一个诡异的bug
        // 就是当前的fragment不显示分享menu（左右滑动发现其他fragment都有分享按钮）。所以只好用这个work-around
        toolbar.inflateMenu(R.menu.menu_detail);
        toolbar.setOnClickListener(v -> scrollView.smoothScrollTo(0, 0));
        fetch();

    }

    @Override
    protected void onAppear() {
        super.onAppear();
        fetch();
    }

    private void cacheComment(EditText editText) {
        if (!TextUtils.isEmpty(commentTemp)) {
            editText.append(commentTemp);
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                commentTemp = s.toString();
            }
        });
    }

    private void comment() {
        commentFragment = BottomDialogFragment.create(R.layout.comment_layout)
                .with(this)
                .bindView(v -> {
                    TextInputLayout textInputLayout = v.findViewById(R.id.commentTextInputLayout);
                    ImageView commit = v.findViewById(R.id.commit);
                    EditText commentEt = textInputLayout.getEditText();
                    assert commentEt != null;
                    cacheComment(commentEt);
                    commit.setOnClickListener(view -> {
                        if (commentEt.getText() == null || commentEt.getText().toString().trim().isEmpty()) {
                            UiUtils.showSnack(view, R.string.no_text_entered);
                        } else {
                            String comment = commentEt.getText().toString();
                            post(comment, 0);
                        }
                    });
                }).listenDismiss(dialog -> {
                    if (!TextUtils.isEmpty(commentTemp)) {
                        UiUtils.showSnack(rootView, getString(R.string.content_saved_as_draft));
                    }

                });
        commentFragment.show();

    }


    private void comment(int recipientId, String userName) {
        commentFragment = BottomDialogFragment.create(R.layout.comment_layout)
                .with(this)
                .bindView(v -> {
                    TextInputLayout textInputLayout = v.findViewById(R.id.commentTextInputLayout);
                    EditText editText = textInputLayout.getEditText();
                    textInputLayout.setHint(String.format(getString(R.string.reply_to_xxx), userName));
                    cacheComment(editText);

                    if (editText != null) {
                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                    }

                    v.findViewById(commit).setOnClickListener(v1 -> {
                        if (TextChecker.isTextInvalid(editText)) {
                            UiUtils.showSnack(v, getString(R.string.no_text_entered));
                        } else {
                            String comment = editText.getText().toString();
                            if (comment.length() < 3) {
                                UiUtils.showSnack(v, getString(R.string.speak_more));
                            } else {
                                post(comment, recipientId);
                            }
                        }
                    });
                }).listenDismiss(dialog -> {
                    if (!TextUtils.isEmpty(commentTemp)) {
                        UiUtils.showSnack(rootView, getString(R.string.content_saved_as_draft));
                    }

                });
        commentFragment.show();

    }

    private void post(String comment, int recipientId) {
        swipeRefresh.setRefreshing(true);
        HashMap<String, Object> data = new HashMap<>();
        data.put(Constants.CONTENT, comment);
        if (recipientId > 0) {
            data.put("recipient_id", recipientId);
        }
        LoginManager.getApi().postDiaryComment(diaryId, data)
                .compose(applySchedulers())
                .subscribe(myComment -> {
                    swipeRefresh.setRefreshing(false);
                    adapter.addData(0, myComment);
                    fetch();

                }, throwable -> {
                    swipeRefresh.setRefreshing(false);
                    UiUtils.showSnackLong(rootView, getString(R.string.comment_failed) + throwable.getMessage());
                });
        commentTemp = null;
        commentFragment.dismiss();
    }

    private void fetch() {
        swipeRefresh.setRefreshing(true);

        TimeApi api = LoginManager.getApi();
        subscription = api.getDiaryDetail(diaryId)
                .zipWith(api.getDiaryComments(diaryId), (d, comments) -> {
                    DiaryDetailFragment.this.diary = d;
                    return comments;
                })
                .compose(applySchedulers())
                .subscribe(comments -> {
                    if (diary == null) {
                        throw new RuntimeException("Diary is null");
                    }
                    swipeRefresh.setRefreshing(false);
                    inflate(comments);
                    setShareIntent(diary.getContent());
//                    base.save(diary);

                }, throwable -> {
                    swipeRefresh.setRefreshing(false);
                    if (!throwable.getMessage().contains("android.app.ActivityOptions.isReturning")) {
                        UiUtils.showSnackLong(rootView, getString(R.string.get_diary_failed) + throwable.getMessage());
                    }
                });
        compositeSubscription.add(subscription);

    }

    @Override
    protected boolean needNavigation() {
        return true;
    }

    private void inflateDiary() {
        toolbar.setTitle(diary.getNotebookSubject());
        myAvatar.setOnClickListener(v -> goProfile(diary.getUserId()));

        if (diary.getUser() != null) {
            Glide.with(this).load(diary.getUser().getAvatarUrl())
                    .bitmapTransform(new RoundedCornersTransformation(getContext(), 5, 0))
                    .into(myAvatar);
        }
        getActivity().supportPostponeEnterTransition();
        diaryDate.setText(DateUtil.getDisplayDay(diary.getCreated()));
        content.setText(diary.getContent());
        diaryLayout.setOnLongClickListener(this);
        content.setOnLongClickListener(this);
        //给名字加蓝
        String name = diary.getUser().getName();
        SimpleText sText = SimpleText.create(getContext(), name)
                .all()
                .textColor(R.color.btg_global_text_blue)
                .onClick((charSequence, range, o) -> {
                    goProfile(diary.getUserId());
                });
        sText.linkify(myName);
        myName.setText(sText);

        time.setText(DateUtil.getDisplayTime(diary.getCreated()));
        initPicture();
    }

    private void initPicture() {
        if (!TextUtils.isEmpty(diary.getPhotoThumbUrl())) {
            boolean gif = diary.getPhotoUrl().endsWith(".gif");
            isGif.setVisibility(View.GONE);
            Glide.with(this).load(diary.getPhotoThumbUrl()).asBitmap().error(R.drawable.error_holder)
                    .listener(new RequestListener<String, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            attachPicture.setVisibility(View.VISIBLE);
                            if (gif) {
                                isGif.setVisibility(View.VISIBLE);
                            }
                            return false;
                        }
                    })
                    .into(attachPicture);
            attachPicture.setOnClickListener(v -> {
                if (gif) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), PictureActivity.class);
                    intent.putExtra("isGif", true);
                    intent.putExtra(Constants.URL, diary.getPhotoUrl());
                    startActivity(intent);
                    return;
                }

                final ProgressBar progressBar = ImageProgresser.attachProgress(attachPicture);
                Glide.with(this).load(diary.getPhotoUrl()).listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        TransitionHelper.startViewer(getActivity(), attachPicture, diary.getPhotoUrl());
                        return false;
                    }
                }).preload();
            });
        }
    }

    private void initFab() {
        new Handler().postDelayed(() -> fab.show(), 200);
        fab.setOnClickListener(v -> comment());
    }


    private void inflate(List<Comment> comments) {
        inflateDiary();
        initFab();
        inflateComments(comments);
        startTransition();

        goCertainComment(comments);
    }

    private void goCertainComment(List<Comment> comments) {
        int commentId = getArguments().getInt(COMMENT_ID);
        if (commentId > 0) {
            for (Comment c : comments) {
                if (c.getId() == commentId) {
                    commentsRecycler.smoothScrollToPosition(comments.indexOf(c));
                    log("goCertainComment index" + comments.indexOf(c));
                }
            }
        }
    }

    private void inflateComments(List<Comment> comments) {
        divider.setVisibility(View.VISIBLE);
        adapter.setNewData(comments);
        if (comments.size() > 0) {
            noComment.setVisibility(View.GONE);
            commentsCount.setVisibility(View.VISIBLE);
            commentsRecycler.setVisibility(View.VISIBLE);
            commentsCount.setText(String.format(getString(R.string.total_comments), diary.getCommentCount()));
        } else {
            noComment.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRefresh() {
        fetch();
    }

    //沒用到
    @Override
    public void onItemClick(int position) {
        goProfile(adapter.getItem(position).getRecipientId());//这里点击的是回复人的名字
    }

    @Override
    public void onItemLongClick(int position) {
        String userName = adapter.getItem(position).getUser().getName();
        ClipboardUtils.copyText(userName);
        UiUtils.showSnack(rootView, String.format(getString(R.string.xxx_copied), userName));
    }

    private void setShareIntent(String shareText) {
        if (mShareActionProvider != null) {
            if (TextUtils.isEmpty(shareText)) {
                diary = base.findDiary(diaryId);
                if (diary != null) shareText = diary.getContent();
            }
            mShareActionProvider.setShareIntent(
                    IntentUtils.getShareTextIntent(Share.appendUrl(shareText)));
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_report) {
            reportDiary(diary);
        }

        return super.onOptionsItemSelected(item);
    }

    private void reportDiary(Diary diary) {
        if (diary == null) return;
        LoginManager.getApi().reportDiary(diary.getUserId(), diary.getId())
                .compose(applySchedulers())
                .subscribe(responseBodyResponse -> {
                    UiUtils.showSnack(rootView, R.string.report_success);
                });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_detail, menu);
        MenuItem item = menu.findItem(R.id.action_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        setShareIntent(null);
    }


    @Override
    public boolean onLongClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (LoginManager.isMe(diary.getUserId())) {
            String[] items = getResources().getStringArray(R.array.diary_menu_my);
            builder.setItems(items, (dialog, which) -> {
                if (which == 0) {
                    editDiary();
                } else if (which == 1) {
                    deleteDiary();
                }
            }).show();
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                onRefresh();
            }
        }
    }

    private void editDiary() {
        Intent intent = new Intent(getContext(), EditDiaryActivity.class);
        intent.putExtra(Constants.ID, diaryId);
        startActivityForResult(intent, 0);
    }

    private void deleteDiary() {
        LoginManager.getApi().deleteDiary(diaryId)
                .compose(applySchedulers())
                .subscribe(responseBodyResponse -> {
                    if (responseBodyResponse.errorBody() == null) {
                        UiUtils.showSnack(content, getString(R.string.diary_delete_success));
                        base.deleteDiary(diaryId);
                        new Handler().postDelayed(() -> getActivity().onBackPressed(), 400);
                        return;
                    }
                    try {
                        String message = responseBodyResponse.errorBody().string();
                        if (!TextUtils.isEmpty(message)) {
                            JSONObject jsonObject = new JSONObject(message);
                            UiUtils.showSnackLong(content, getString(R.string.diary_delete_failed)
                                    + " " + jsonObject.optString("message"));
                        }

                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }, new HttpErrorAction<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        super.call(throwable);
                        UiUtils.showSnackLong(content, getString(R.string.diary_delete_failed) + " " + errorMessage);
                    }
                });
    }

}



