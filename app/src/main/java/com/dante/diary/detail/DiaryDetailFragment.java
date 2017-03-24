package com.dante.diary.detail;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blankj.utilcode.utils.ClipboardUtils;
import com.blankj.utilcode.utils.IntentUtils;
import com.blankj.utilcode.utils.KeyboardUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.dante.diary.R;
import com.dante.diary.base.BaseFragment;
import com.dante.diary.base.Constants;
import com.dante.diary.custom.BottomCommentFragment;
import com.dante.diary.custom.RevealHelper;
import com.dante.diary.interfaces.IOnItemClickListener;
import com.dante.diary.login.LoginManager;
import com.dante.diary.model.Comment;
import com.dante.diary.model.Diary;
import com.dante.diary.net.TimeApi;
import com.dante.diary.utils.DateUtil;
import com.dante.diary.utils.ImageProgresser;
import com.dante.diary.utils.Imager;
import com.dante.diary.utils.Share;
import com.dante.diary.utils.SpUtil;
import com.dante.diary.utils.TextChecker;
import com.dante.diary.utils.TransitionHelper;
import com.dante.diary.utils.UiUtils;
import com.dante.diary.utils.WrapContentLinearLayoutManager;
import com.jaychang.st.SimpleText;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * A simple {@link Fragment} subclass.
 */
public class DiaryDetailFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, IOnItemClickListener {

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
    @BindView(R.id.reveal)
    LinearLayout reveal;
    @BindView(R.id.commentEt)
    TextInputEditText commentEt;
    @BindView(R.id.commentTextInputLayout)
    TextInputLayout commentTextInputLayout;
    @BindView(R.id.commit)
    ImageButton commit;
    private ShareActionProvider mShareActionProvider;
    private int diaryId;
    private String commentTemp;
    private BottomCommentFragment commentFragment;
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
        if (getArguments() != null) {  //有参数则获取参数id
            diaryId = getArguments().getInt(Constants.ID);
        }
        setHasOptionsMenu(true);//填充menu（执行onCreateOptionsMenu）
        adapter = new DiaryCommentsAdapter(null, this);
        layoutManager = new WrapContentLinearLayoutManager(getContext());
        commentsRecycler.setLayoutManager(layoutManager);
        commentsRecycler.setNestedScrollingEnabled(false);//recyclerView在NestedScrollView中顺滑的秘诀
        commentsRecycler.setAdapter(adapter);
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
//                    comment(comment.getUserId(), comment.getFollowUser().getName());
                }
            }

            @Override
            public void onSimpleItemClick(BaseQuickAdapter quickAdapter, View view, int position) {
                Comment comment = adapter.getItem(position);
                comment(comment.getUserId(), comment.getUser().getName());
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
                    String r = null;
                    try {
                        r = responseBodyResponse.body().string();
                        //nothing here
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, throwable -> {
                    UiUtils.showSnack(rootView, R.string.delete_comment_failed);
                    throwable.printStackTrace();
                });
    }


    @Override
    protected void initData() {
        setToolbarScrollFlag(false);
        //本来 onCreateOptionsMenu 已填充了分享Menu，但是却遇到了一个诡异的bug
        // 就是当前的fragment不显示分享menu（左右滑动发现其他fragment都有分享按钮）。所以只好用这个work-around
        toolbar.inflateMenu(R.menu.share_menu);
        fetch();

    }

    @Override
    protected void onAppear() {
        super.onAppear();
        fetch();
    }

//    private void comment() {
//        commentFragment = BottomCommentFragment.create(R.layout.comment_layout).with(this)
//                .bindView(v -> {
//
//                    EditText editText = (EditText) v.findViewById(commentEt);
//                    View commit = v.findViewById(R.id.commit);
//
//
//                })
//                .listenDismiss(dialog -> {
//
//                });
////        commentFragment.show();
//
//    }

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

    private void comment(int recipientId, String userName) {
        commentFragment = BottomCommentFragment.create(R.layout.comment_layout)
                .with(this)
                .bindView(v -> {
                    TextInputLayout textInputLayout = (TextInputLayout) v.findViewById(R.id.commentTextInputLayout);
                    EditText editText = textInputLayout.getEditText();
                    textInputLayout.setHint(String.format(getString(R.string.reply_to_xxx), userName));
                    cacheComment(editText);

                    if (editText != null) {
                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                    }

                    v.findViewById(R.id.commit).setOnClickListener(v1 -> {
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
                    commentTemp = null;

                }, throwable -> {
                    swipeRefresh.setRefreshing(false);
                    UiUtils.showSnackLong(rootView, R.string.comment_failed);
                    throwable.printStackTrace();
                });
    }

    private void fetch() {
        swipeRefresh.setRefreshing(true);

        TimeApi api = LoginManager.getApi();
        subscription = api.getDiaryDetail(diaryId)
                .zipWith(api.getDiaryComments(diaryId), (diary1, comments) -> {
                    DiaryDetailFragment.this.diary = diary1;
                    return comments;
                })
                .compose(applySchedulers())
                .subscribe(comments -> {
                    swipeRefresh.setRefreshing(false);
                    inflate(comments);
                    setShareIntent(diary.getContent());

                }, throwable -> {
                    swipeRefresh.setRefreshing(false);
                    UiUtils.showSnackLong(rootView, R.string.get_diary_failed);
                    throwable.printStackTrace();
                });
        compositeSubscription.add(subscription);

    }

    @Override
    protected boolean needNavigation() {
        return true;
    }

    private void inflateDiary() {
        toolbar.setTitle(diary.getNotebookSubject());

        Imager.load(this, diary.getUser().getAvatarUrl(), myAvatar);
        myAvatar.setOnClickListener(v -> goProfile(diary.getUserId()));

        Glide.with(this).load(diary.getUser().getAvatarUrl())
                .bitmapTransform(new RoundedCornersTransformation(getContext(), 5, 0))
                .into(myAvatar);

        diaryDate.setText(DateUtil.getDisplayDay(diary.getCreated()));
        content.setText(diary.getContent());
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
            attachPicture.setVisibility(View.VISIBLE);
            attachPicture.setOnClickListener(v -> {
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

            Imager.load(this, diary.getPhotoThumbUrl(), attachPicture);
        }
    }

    private void initFab() {
        fab.show();
//        fab.setOnClickListener(v -> comment());
        RevealHelper helper = RevealHelper.with(getActivity()).reveal(reveal)
                .revealDuration(300)
                .buttonTransitionDuration(200)
                .button(fab)
                .onRevealEnd(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        KeyboardUtils.showSoftInput(commentEt);
                        cacheComment(commentEt);
                        commit.setOnClickListener(view -> {
                            if (commentEt.getText() == null || commentEt.getText().toString().trim().isEmpty()) {
                                UiUtils.showSnack(view, R.string.no_text_entered);
                            } else {
                                String comment = commentEt.getText().toString();
                                post(comment, 0);
                            }
                        });
                    }
                })
                .build();

        commentEt.setOnFocusChangeListener((v, hasFocus) -> {
            Log.d(TAG, "onFocusChange: " + hasFocus);
            if (!hasFocus) {
                InputMethodManager imm =  (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                helper.unreveal();
                if (!TextUtils.isEmpty(commentTemp)) {
                    UiUtils.showSnack(rootView, getString(R.string.content_saved_as_draft));
                }
            }
        });

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
            Log.d(TAG, "setShareIntent: " + shareText);
            mShareActionProvider.setShareIntent(
                    IntentUtils.getShareTextIntent(Share.appendUrl(shareText)));
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.share_menu, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        setShareIntent(null);
    }


}



