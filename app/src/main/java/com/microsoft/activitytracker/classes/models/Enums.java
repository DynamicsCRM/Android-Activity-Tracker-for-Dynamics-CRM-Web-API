package com.microsoft.activitytracker.classes.models;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Enums {

    public static final String CHECKIN = "checkin";
    public static final String PHONECALL = "phonecall";
    public static final String FOLLOWUP = "followup";
    public static final String ANNOTATION = "annotation";

    @StringDef({CHECKIN, PHONECALL, FOLLOWUP, ANNOTATION})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CreateTypes {}

    @CreateTypes
    public static String getCreateType(String type) {
        switch(type) {
            case CHECKIN:
                return CHECKIN;
            case PHONECALL:
                return PHONECALL;
            case FOLLOWUP:
                return FOLLOWUP;
            case ANNOTATION:
                return ANNOTATION;
        }

        return null;
    }

}
