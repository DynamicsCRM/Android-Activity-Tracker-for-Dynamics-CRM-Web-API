package com.microsoft.activitytracker.data;

import com.raizlabs.android.dbflow.annotation.Database;

@Database(name = TrackerDatabase.NAME, version = TrackerDatabase.VERSION)
public class TrackerDatabase {

    public static final int VERSION = 1;
    public static final String NAME = "trackerdb";

}
