package com.microsoft.activitytracker;

import com.facebook.stetho.Stetho;
import com.microsoft.activitytracker.classes.ActivityTracker;

public final class DebugActivityTracker extends ActivityTracker {

    @Override
    public void onCreate() {
        super.onCreate();

        Stetho.initialize(
            Stetho.newInitializerBuilder(this)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                .build());

    }
}