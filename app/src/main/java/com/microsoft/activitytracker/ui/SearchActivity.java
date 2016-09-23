package com.microsoft.activitytracker.ui;

import android.app.SearchManager;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;

import com.microsoft.activitytracker.R;
import com.microsoft.activitytracker.classes.models.Entity;
import com.microsoft.activitytracker.data.DataLoadingSubject;
import com.microsoft.activitytracker.data.loaders.SearchLoader;
import com.microsoft.activitytracker.databinding.ActivityMainBinding;
import com.microsoft.activitytracker.ui.adapters.MainAdapter;

import java.util.List;

public final class SearchActivity extends AppCompatActivity implements DataLoadingSubject.DataLoadingCallbacks, SwipeRefreshLayout.OnRefreshListener {

    private ActivityMainBinding binding;
    private ActionBar actionBar;
    private SearchLoader loader;
    private MainAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setSupportActionBar(binding.appbar.toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.itemsList.setLayoutManager(layoutManager);
        binding.itemsList.setItemAnimator(new DefaultItemAnimator());

        binding.refreshLayout.setRefreshing(true);
        binding.refreshLayout.setOnRefreshListener(this);

        adapter = new MainAdapter(this);
        binding.itemsList.setAdapter(adapter);

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void createDataLoader() {
        loader = new SearchLoader(this, true) {
            @Override
            public void onDataLoaded(List<Entity> retrieveResponse) {
                adapter.updateCollection(retrieveResponse);
            }
        };

        loader.registerCallback(this);
    }

    private void handleIntent(Intent intent) {
        if (loader == null) {
            createDataLoader();
        }

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            actionBar.setTitle(query);
            loader.searchFor(query);
        }
    }

    @Override
    public void dataStartedLoading() {
        binding.refreshLayout.setRefreshing(true);
    }

    @Override
    public void dataFinishedLoading() {
        binding.refreshLayout.setRefreshing(false);

        if (adapter.getItemCount() == 0) {
            Snackbar
                    .make(binding.mainContent, "No results were found", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void dataFailedLoading(String errorMessage) {
        Snackbar.make(binding.mainContent, errorMessage, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRefresh() {
        adapter.setToClear(true);
        handleIntent(getIntent());
    }
}
