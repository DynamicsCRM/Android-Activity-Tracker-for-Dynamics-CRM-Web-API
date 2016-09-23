package com.microsoft.activitytracker.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.activitytracker.R;
import com.microsoft.activitytracker.classes.ActivityTracker;
import com.microsoft.activitytracker.classes.modules.LoginModule;
import com.microsoft.activitytracker.classes.modules.StorageModule;
import com.microsoft.activitytracker.data.DataLoadingSubject;
import com.microsoft.activitytracker.data.loaders.DefinitionsLoader;
import com.microsoft.activitytracker.databinding.ActivitySetupBinding;


import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public final class SetupActivity extends Activity implements View.OnClickListener, AuthenticationCallback<AuthenticationResult>,DataLoadingSubject.DataLoadingCallbacks {

    private static final String REDIRECT_URI = "http://crm.codesamples/";
    static final String CLIENT_ID = "1dc3cd16-85f4-449e-9145-98c996ea6a85";

    @Inject SharedPreferences sharedPreferences;
    @Inject @Named(StorageModule.ENDPOINT) String endpoint;
    @Inject @Named(StorageModule.USERNAME) String username;
    @Inject @Named(StorageModule.AUTHORITY) String authority;

    private ActivitySetupBinding binding;
    private AuthenticationContext authContext;
    private ProgressDialog progressDialog;
    private DefinitionsLoader loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setup);
        ButterKnife.bind(this);

        ActivityTracker.openNewStorage().inject(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        binding.endpointText.setText(endpoint);
    }

    private boolean isConnected() {
        ConnectivityManager manager = (ConnectivityManager)this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    @OnClick(R.id.login_button)
    public void onClick(View v) {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        if (!isConnected()) {
            Snackbar.make(binding.coordinatedContainer, getString(R.string.network_error),
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        if (binding.endpointText.getText().toString().equals("")) {
            Snackbar
                .make(binding.coordinatedContainer, getString(R.string.endpoint_required),
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        if (!Patterns.WEB_URL.matcher(binding.endpointText.getText().toString()).matches()) {
            Snackbar
                .make(binding.coordinatedContainer, getString(R.string.invalid_endpoint),
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        progressDialog.setMessage("Challenging Organization...");
        progressDialog.show();

        endpoint = binding.endpointText.getText().toString();
        username = binding.usernameText.getText().toString();
        LoginModule.AuthorityService service = ActivityTracker.createNewLoginSession(endpoint).getEndpoint();

        service.getAuthority()
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError(throwable -> Snackbar.make(binding.coordinatedContainer, throwable.getMessage(),
                    Snackbar.LENGTH_LONG).show())
            .doOnNext(response -> {
                List<String> authorityHeaders = response.raw().headers("WWW-Authenticate");
                for (int i = 0; i < authorityHeaders.size(); i++) {
                    String authorityHeader = authorityHeaders.get(i);
                    if (!authorityHeader.equals("")) {
                        String headerSubstring;
                        if (!authorityHeader.contains("https://")) {
                            continue;
                        } else if (authorityHeader.indexOf("https://") != authorityHeader.lastIndexOf("https://")) {
                            headerSubstring = authorityHeader.substring(authorityHeader.indexOf("https://"), authorityHeader.indexOf(","));
                        } else {
                            headerSubstring = authorityHeader.substring(authorityHeader.indexOf("https://"));
                        }

                        authority = headerSubstring;
                        sharedPreferences.edit().putString(StorageModule.AUTHORITY, authority).apply();
                        break;
                    }
                }
            })
            .doOnCompleted(() -> {
                if (authority != null && !authority.equals("")) {
                    login();
                }
                else {
                    Snackbar.make(binding.coordinatedContainer, getString(R.string.authority_error),
                            Snackbar.LENGTH_LONG).show();
                }
            })
            .subscribe();
    }

    private void login() {
        progressDialog.setMessage("Logging In...");

        try {
            authContext = new AuthenticationContext(SetupActivity.this, authority, false);
            authContext.acquireToken(SetupActivity.this, endpoint, CLIENT_ID, REDIRECT_URI, username, this);
        }
        catch(Exception ex) {
            Log.e("SignInActivity", ex.getCause().getMessage());

            Snackbar
                .make(binding.coordinatedContainer, "Error Logging In", Snackbar.LENGTH_LONG)
                .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (authContext != null) {
            if (resultCode != 2001) {
                authContext.onActivityResult(requestCode, resultCode, data);
            }
            else {
                final CookieManager cookieManager = CookieManager.getInstance();
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    CookieSyncManager.createInstance(this);
                    cookieManager.removeAllCookie();
                    CookieSyncManager.createInstance(this).sync();

                    authContext.getCache().removeAll();
                }
                else {
                    cookieManager.removeAllCookies(value -> authContext.getCache().removeAll());
                }
            }
        }
    }

    /**
     * If the login is successful, save the current token into the property created for the application,
     * store the username and endpoint into application shared storage, and return to the main activity
     * @param result returns <T> the result of the authentication
     */
    @Override
    public void onSuccess(AuthenticationResult result) {
        ActivityTracker.loginOrgServices(endpoint, result.getAccessToken());

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(StorageModule.TOKEN, result.getAccessToken());
        editor.putLong(StorageModule.EXPIRES_ON, result.getExpiresOn().getTime());
        editor.putString(StorageModule.REFRESH_TOKEN, result.getRefreshToken());
        editor.putString(StorageModule.ENDPOINT, endpoint);
        editor.putString(StorageModule.USERNAME, username);

        editor.apply();

        loader = new DefinitionsLoader(this);
        loader.registerCallback(this);
        loader.getRequiredMetadataDefinitions();
    }

    @Override
    public void onError(Exception ex) {
        Log.e("SignInActivity", ex.getCause().getMessage());
    }

    @Override
    public void onBackPressed() {
        // since we require them to be logged in for everything,
        // user can't leave this page unless they successfully log in.
    }

    @Override
    public void dataStartedLoading() {
        progressDialog.setMessage("Updating Entity Metadata...");
    }

    @Override
    public void dataFinishedLoading() {
        progressDialog.setOnDismissListener(dialog -> finish());
        progressDialog.dismiss();
    }

    @Override
    public void dataFailedLoading(String errorMessage) {
        progressDialog.setOnDismissListener(dialog ->
            Snackbar.make(binding.coordinatedContainer, errorMessage, Snackbar.LENGTH_LONG)
                    .show());
        progressDialog.dismiss();
    }
}
