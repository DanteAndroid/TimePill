package com.dante.diary.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.blankj.utilcode.utils.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.dante.diary.R;
import com.dante.diary.base.Constants;
import com.dante.diary.base.RecyclerFragment;
import com.dante.diary.base.ViewActivity;
import com.dante.diary.edit.EditNotebookActivity;
import com.dante.diary.login.LoginManager;
import com.dante.diary.model.Notebook;
import com.dante.diary.net.HttpErrorAction;
import com.dante.diary.utils.UiUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import butterknife.BindView;
import io.realm.RealmResults;

/**
 * Created by yons on 17/3/9.
 */

public class NoteBookListFragment extends RecyclerFragment {
    private static final String TAG = "DiaryListFragment";
    StaggeredGridLayoutManager layoutManager;
    NotebookListAdapter adapter;
    RealmResults<Notebook> notebooks;
    @BindView(R.id.stateText)
    TextView stateText;

    private int page;
    private int userId;

    public static NoteBookListFragment newInstance(int userId) {

        Bundle args = new Bundle();
        args.putInt(Constants.ID, userId);
        NoteBookListFragment fragment = new NoteBookListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initViews() {
        super.initViews();
        adapter = new NotebookListAdapter(null);
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                onNotebookClicked(view, i);
            }

            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                int id = view.getId();
                if (id == R.id.more) {
                    moreClicked(view, position);
                }

            }

            @Override
            public void onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                onNotebookLongClicked(view, position);
            }
        });

    }

    private void onNotebookLongClicked(View view, int position) {
        Notebook n = adapter.getItem(position);
//        showNotebookEditDialog(cover, position);
        new AlertDialog.Builder(getActivity()).setItems(R.array.actions, (dialog, which) -> {
            if (which == 0) {
                View cover = view.findViewById(R.id.cover);
                showNotebookEditDialog(cover, position);
            } else if (which == 1) {
                deleteNotebook(view, position, n);
            }
        }).show();

    }

    private void deleteNotebook(View view, int position, Notebook n) {
        new AlertDialog.Builder(getActivity()).setMessage(
                String.format(Locale.getDefault(), getString(R.string.delete_notebook_hint), n.getSubject()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    LoginManager.getApi().deleteNotebook(n.getId())
                            .compose(applySchedulers())
                            .subscribe(responseBodyResponse -> {
                                try {
                                    String error = responseBodyResponse.errorBody().string();
                                    if (TextUtils.isEmpty(error)) {
                                        adapter.remove(position);
                                        UiUtils.showSnack(barActivity.bottomBar, R.string.delete_notebook_success);
                                    } else {
                                        String errorMessage = "";
                                        try {
                                            errorMessage = new JSONObject(error).optString("message");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        ToastUtils.showShortToast(getString(R.string.delete_notebook_failed) + " " + errorMessage);
                                    }

                                } catch (NullPointerException e) {
                                    ToastUtils.showShortToast(getString(R.string.delete_notebook_success));
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }, new HttpErrorAction<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    super.call(throwable);
                                    ToastUtils.showShortToast(getString(R.string.delete_notebook_failed) + " " + errorMessage);
                                }
                            });
                })
                .setNegativeButton(R.string.nope, null)
                .show();
    }

    @Override
    protected boolean hasFab() {
        return true;
    }

    private void moreClicked(View view, int position) {
        Notebook n = adapter.getItem(position);
        PopupMenu popup = new PopupMenu(getContext(), view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_more, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    ViewGroup parent = (ViewGroup) view.getParent().getParent();
                    View cover = parent.findViewById(R.id.cover);
                    showNotebookEditDialog(cover, position);
                    break;
                case R.id.action_delete:
                    deleteNotebook(view, position, n);
                    break;
                default:
                    break;
            }
            return false;
        });
        popup.show();
    }

    private void onNotebookClicked(View view, int index) {
        Notebook n = adapter.getItem(index);
        ViewActivity.viewDiaryList(getActivity(), n.getId(), n.getSubject());
    }

    @Override
    protected void initData() {
        super.initData();
        userId = getArguments().getInt(Constants.ID);
        if (userId <= 0) {
            UiUtils.showSnack(rootView, R.string.get_profile_failed);
            return;
        }

        notebooks = base.findNotebooks(userId);
        adapter.setNewData(notebooks);
        fetch();
    }


    protected void fetch() {
        changeRefresh(true);
        subscription = LoginManager.getApi()
                .getMyNotebooks(userId)
                .compose(applySchedulers())
                .subscribe(notebooks -> {
                    if (notebooks.isEmpty()) {
                        if (page <= 1) {
                            stateText.setText(R.string.no_notebook);
                            stateText.setVisibility(View.VISIBLE);
                        }
                    } else {
                        stateText.setVisibility(View.GONE);
                        adapter.setNewData(notebooks);
                    }
                    base.save(notebooks);
                    changeRefresh(false);
                }, throwable -> Log.e("test", "fetch: " + throwable.getMessage()));
    }

    @Override
    public void onRefresh() {
        fetch();
    }


    private void showNotebookEditDialog(View cover, int position) {
        Notebook notebook = adapter.getItem(position);
        if (notebook == null || !LoginManager.isMe(notebook.getUserId())) return;
        int id = notebook.getId();

        ViewCompat.setTransitionName(cover, String.valueOf(id));
        Intent intent = new Intent(getContext().getApplicationContext(), EditNotebookActivity.class);
        intent.putExtra(Constants.ID, id);
        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(getActivity(), cover, String.valueOf(id));
        ActivityCompat.startActivity(getContext(), intent, options.toBundle());

    }
}
