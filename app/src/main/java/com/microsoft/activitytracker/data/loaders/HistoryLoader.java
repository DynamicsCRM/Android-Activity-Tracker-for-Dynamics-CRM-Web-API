package com.microsoft.activitytracker.data.loaders;

import android.content.Context;

import com.microsoft.activitytracker.classes.models.Entity;
import com.microsoft.activitytracker.data.BaseDataManager;
import com.microsoft.activitytracker.data.storage.HistoryEntry;

import java.util.List;

public abstract class HistoryLoader extends BaseDataManager {

    public HistoryLoader(Context context) {
        super(context);
    }

    public abstract void onHistoryLoaded(List<Entity> entities);

    public void loadOpenedHistory() {
        loadStarted();
        incrementLoadingCount();

        onHistoryLoaded(HistoryEntry.getHistoryEntries());

        decrementLoadingCount();
        loadFinished();
    }
}
