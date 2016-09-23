package com.microsoft.activitytracker.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.microsoft.activitytracker.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseDataManager implements DataLoadingSubject {

    private Context context;
    private AtomicInteger loadingCount;
    private List<DataLoadingCallbacks> loadingCallbacks;

    public BaseDataManager(Context context) {
        this.context = context;
        loadingCount = new AtomicInteger(0);
    }

    @Override
    public boolean isDataLoading() {
        return loadingCount.get() > 0;
    }

    protected boolean isConnected() {
        ConnectivityManager manager = (ConnectivityManager)context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            loadFailed(context.getString(R.string.network_error));
        }

        return isConnected;
    }

    protected void loadStarted() {
        dispatchLoadingStartedCallbacks();
    }

    protected void loadFinished() {
        dispatchLoadingFinishedCallbacks();
    }

    protected void loadFailed(String errorMessage) {
        dispatchLoadingFailedCallbacks(errorMessage);
    }

    protected void resetLoadingCount() {
        loadingCount.set(0);
    }

    protected void incrementLoadingCount() {
        loadingCount.incrementAndGet();
    }

    protected void incrementLoadingCount(int count) {
        loadingCount.set(count);
    }

    protected void decrementLoadingCount() {
        loadingCount.decrementAndGet();
    }

    @Override
    public void registerCallback(DataLoadingSubject.DataLoadingCallbacks callback) {
        if (loadingCallbacks == null) {
            loadingCallbacks = new ArrayList<>(1);
        }

        loadingCallbacks.add(callback);
    }

    @Override
    public void unregisterCallback(DataLoadingSubject.DataLoadingCallbacks callback) {
        if (loadingCallbacks.contains(callback)) {
            loadingCallbacks.remove(callback);
        }
    }

    protected void dispatchLoadingStartedCallbacks() {
        if (loadingCount.intValue() == 0) {
            if (loadingCallbacks != null && !loadingCallbacks.isEmpty()) {
                for (DataLoadingCallbacks loadingCallback : loadingCallbacks) {
                    loadingCallback.dataStartedLoading();
                }
            }
        }
    }

    protected void dispatchLoadingFinishedCallbacks() {
        if (loadingCount.intValue() == 0) {
            if (loadingCallbacks != null && !loadingCallbacks.isEmpty()) {
                for (DataLoadingCallbacks loadingCallback : loadingCallbacks) {
                    loadingCallback.dataFinishedLoading();
                }
            }
        }
    }

    protected void dispatchLoadingFailedCallbacks(String errorMessage) {
        if (loadingCallbacks != null && !loadingCallbacks.isEmpty()) {
            for (DataLoadingCallbacks loadingCallback : loadingCallbacks) {
                loadingCallback.dataFailedLoading(errorMessage);
            }
        }
    }
}
