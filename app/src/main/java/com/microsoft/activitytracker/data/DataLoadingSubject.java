package com.microsoft.activitytracker.data;

public interface DataLoadingSubject {

    boolean isDataLoading();

    void registerCallback(DataLoadingCallbacks callbacks);
    void unregisterCallback(DataLoadingCallbacks callbacks);

    interface DataLoadingCallbacks {
        void dataStartedLoading();
        void dataFinishedLoading();
        void dataFailedLoading(String errorMessage);
    }

}
