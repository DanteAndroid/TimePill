package com.dante.diary.search;

import android.app.SearchManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.blankj.utilcode.utils.KeyboardUtils;
import com.dante.diary.R;
import com.dante.diary.base.BaseActivity;
import com.dante.diary.base.Constants;
import com.dante.diary.profile.DiaryListFragment;

import rx.Observable;

public class SearchActivity extends BaseActivity {
    public static final int SEARCH_DEBOUNCE_TIME = 5000;
    private static final String TAG = "SearchActivity";
    private long nowTime;
    private long lastTime;
    private SearchView searchView;
    private Fragment fragment;
    private int notebookId;
    private String notebookSubject;
    private String keywords;
    private MenuItem searchItem;
    private boolean hasInit;


    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        if (getIntent() != null) {
            notebookId = getIntent().getIntExtra(Constants.ID, 0);
            notebookSubject = getIntent().getStringExtra(Constants.SUBJECT);
        }
    }


    private void initSearchView() {
        if (hasInit) {
            return;
        }
        String hint;
        if (TextUtils.isEmpty(notebookSubject)) {
            hint = getString(R.string.search_diary_hint);
        } else {
            hint = String.format(getString(R.string.search_notebook_diary_hint), notebookSubject);
        }
        searchView.setQueryHint(hint);
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> Log.d(TAG, "onFocusChange: " + hasFocus));
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                nowTime = System.currentTimeMillis();
                Observable.just(newText).filter(s -> !s.isEmpty()
                        && nowTime - lastTime > SEARCH_DEBOUNCE_TIME)
                        .subscribe(s -> fetchResult(s));
                return false;
            }

            public boolean onQueryTextSubmit(String query) {
                searchView.setQuery(query, false);
                fetchResult(query);
                KeyboardUtils.hideSoftInput(SearchActivity.this);
                return true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);
        searchItem.expandActionView();
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                KeyboardUtils.hideSoftInput(SearchActivity.this);
                new Handler().postDelayed(() -> finish(), 160);
                return true;
            }
        });
        hasInit = true;
    }

    private void fetchResult(String keywords) {
        this.keywords = keywords;
        fragment = DiaryListFragment.newInstance(notebookId, DiaryListFragment.SEARCH, keywords);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        lastTime = nowTime;
    }


    @Override
    protected int initLayoutId() {
        return R.layout.activity_search;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        // Retrieve the SearchView and plug it into SearchManager
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        initSearchView();
        return true;
    }
}
