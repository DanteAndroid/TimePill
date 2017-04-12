package com.dante.diary.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.dante.diary.R;
import com.dante.diary.base.Constants;
import com.dante.diary.base.RecyclerFragment;
import com.dante.diary.base.ViewActivity;
import com.dante.diary.edit.EditNotebookActivity;
import com.dante.diary.login.LoginManager;
import com.dante.diary.model.Notebook;
import com.dante.diary.utils.UiUtils;

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
        });

    }

    @Override
    protected boolean hasFab() {
        return true;
    }

    private void moreClicked(View view, int position) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_more, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    showNotebookEditDialog(view, position);
                    break;
                default:
                    break;
            }
            return false;
        });
        popup.show();
    }

    private void onNotebookClicked(View view, int index) {
        int id = adapter.getItem(index).getId();
        String subject = adapter.getItem(index).getSubject();
//        Fragment f = DiaryListFragment.newInstance(userId, subject);
//        add(f);
        ViewActivity.viewDiaryList(getActivity(), id, subject);
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
                    if (page <= 1 && notebooks.isEmpty()) {
                        stateText.setText(R.string.no_notebook);
                        stateText.setVisibility(View.VISIBLE);
                    } else {
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


    private void showNotebookEditDialog(View view, int position) {
        ViewGroup parent = (ViewGroup) view.getParent().getParent();
        View cover = parent.findViewById(R.id.cover);
        int id = adapter.getItem(position).getId();

        ViewCompat.setTransitionName(cover, String.valueOf(id));
        Intent intent = new Intent(getContext().getApplicationContext(), EditNotebookActivity.class);
        intent.putExtra(Constants.ID, id);
        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(getActivity(), cover, String.valueOf(id));
        ActivityCompat.startActivity(getContext(), intent, options.toBundle());


    }
}
