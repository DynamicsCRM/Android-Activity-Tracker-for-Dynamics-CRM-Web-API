package com.microsoft.activitytracker.ui;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationContext;

import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.activitytracker.R;
import com.microsoft.activitytracker.classes.ActivityTracker;
import com.microsoft.activitytracker.classes.models.Entity;
import com.microsoft.activitytracker.classes.modules.StorageModule;
import com.microsoft.activitytracker.data.DataLoadingSubject;
import com.microsoft.activitytracker.data.loaders.HistoryLoader;
import com.microsoft.activitytracker.data.storage.HistoryEntry;
import com.microsoft.activitytracker.databinding.ActivityMainBinding;
import com.microsoft.activitytracker.ui.adapters.MainAdapter;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public final class MainActivity extends AppCompatActivity implements DataLoadingSubject.DataLoadingCallbacks,
        SwipeRefreshLayout.OnRefreshListener {

    private static final int SETUP_ID = 0;

    @Inject SharedPreferences sharedPreferences;
    @Inject long expiresOn;
    @Inject @Named(StorageModule.TOKEN) String token;
    @Inject @Named(StorageModule.REFRESH_TOKEN) String refreshToken;
    @Inject @Named(StorageModule.USERNAME) String username;
    @Inject @Named(StorageModule.ENDPOINT) String endpoint;
    @Inject @Named(StorageModule.AUTHORITY) String authority;

    private ActivityMainBinding binding;
    private AuthenticationContext authContext;
    private HistoryLoader loader;
    private MainAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(binding.appbar.toolbar);

        adapter = new MainAdapter(this);
        binding.itemsList.setItemAnimator(new DefaultItemAnimator());
        binding.itemsList.setLayoutManager(new LinearLayoutManager(this));
        binding.itemsList.setAdapter(adapter);

        loader = new HistoryLoader(this) {
            @Override
            public void onHistoryLoaded(List<Entity> entities) {
                adapter.updateCollection(entities);
            }
        };

        binding.refreshLayout.setOnRefreshListener(this);
        loader.registerCallback(this);
    }

    private void refreshStorageData() {
        ActivityTracker.openNewStorage().inject(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupActivity();
    }

    public void setupActivity() {
        refreshStorageData();
        if (ActivityTracker.getOrgServiceComponent() == null || expiresOn - new Date().getTime() < 900000) {
            Authentication();
        }

        adapter.setToClear(true);
        loader.loadOpenedHistory();
    }

    AuthenticationCallback<AuthenticationResult> authCallback = new AuthenticationCallback<AuthenticationResult>() {
        @Override
        public void onSuccess(AuthenticationResult result) {
            token = result.getAccessToken();
            refreshToken = result.getRefreshToken();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(StorageModule.EXPIRES_ON, result.getExpiresOn().getTime());
            editor.putString(StorageModule.TOKEN, token);
            editor.putString(StorageModule.REFRESH_TOKEN, refreshToken);
            editor.apply();

            ActivityTracker.loginOrgServices(endpoint, token);
            refreshStorageData();
        }

        @Override
        public void onError(Exception exc) {

        }
    };

    private void startSignIn() {
        Intent setup = new Intent(MainActivity.this, SetupActivity.class);
        startActivityForResult(setup, SETUP_ID);
    }

    private void Authentication() {
        try {
            if (!authority.equals("") && !username.equals("")) {
                createAuthContext();
                if (expiresOn - new Date().getTime() < 900000) {
                    if (!refreshToken.equals("")) {
                        authContext.acquireTokenByRefreshToken(refreshToken, SetupActivity.CLIENT_ID, authCallback);
                    } else {
                        authContext.acquireTokenSilentAsync(endpoint, SetupActivity.CLIENT_ID, username, authCallback);
                    }
                }
                else {
                    ActivityTracker.loginOrgServices(endpoint, token);
                }
            }
            else {
                startSignIn();
            }
        } catch(Exception ex) {
            Log.e("MainActivity", ex.getCause().getMessage());
            startSignIn();
        }
    }

    private void createAuthContext() {
        try {
            authContext = new AuthenticationContext(MainActivity.this, authority, false);
        }
        catch(Exception ex) {
            Log.e("MainActivity", ex.getCause().getMessage());
            Snackbar
                .make(binding.mainContent, "Error setting up Active Directory", Snackbar.LENGTH_LONG)
                .show();
        }
    }

    @Override
    public void onRefresh() {
        adapter.setToClear(true);
        loader.loadOpenedHistory();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SETUP_ID:
                refreshStorageData();
                loader.loadOpenedHistory();
                break;
            default:
                if (authContext != null) {
                    authContext.onActivityResult(requestCode, resultCode, data);
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(new ComponentName(getApplication(), SearchActivity.class)));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.logout:
                SharedPreferences.Editor editPrefs = sharedPreferences.edit();
                editPrefs.remove(StorageModule.ENDPOINT);
                editPrefs.remove(StorageModule.REFRESH_TOKEN);
                editPrefs.remove(StorageModule.USERNAME);
                editPrefs.remove(StorageModule.AUTHORITY);

                createAuthContext();

                final CookieManager cookieManager = CookieManager.getInstance();
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    CookieSyncManager.createInstance(this);
                    cookieManager.removeAllCookie();
                    CookieSyncManager.createInstance(this).sync();
                }
                else {
                    cookieManager.removeAllCookies(value -> authContext.getCache().removeAll());
                }

                HistoryEntry.clearRecentRecords();
                editPrefs.apply();

                authContext.getCache().removeAll();
                Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                startActivityForResult(setupIntent, SETUP_ID);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void dataStartedLoading() {
        binding.refreshLayout.setRefreshing(true);
    }

    @Override
    public void dataFinishedLoading() {
        binding.refreshLayout.setRefreshing(false);
    }

    @Override
    public void dataFailedLoading(String errorMessage) {
        Snackbar
            .make(binding.mainContent, errorMessage, Snackbar.LENGTH_LONG)
            .show();
    }
}