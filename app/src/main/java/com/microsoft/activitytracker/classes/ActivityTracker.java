package com.microsoft.activitytracker.classes;

import android.app.Application;

import com.microsoft.activitytracker.classes.components.BaseComponent;
import com.microsoft.activitytracker.classes.components.DaggerBaseComponent;
import com.microsoft.activitytracker.classes.components.DaggerLoginComponent;
import com.microsoft.activitytracker.classes.components.DaggerOrgServiceComponent;
import com.microsoft.activitytracker.classes.components.DaggerStorageComponent;
import com.microsoft.activitytracker.classes.components.LoginComponent;
import com.microsoft.activitytracker.classes.components.OrgServiceComponent;
import com.microsoft.activitytracker.classes.components.StorageComponent;
import com.microsoft.activitytracker.classes.modules.BaseModule;
import com.microsoft.activitytracker.classes.modules.LoginModule;
import com.microsoft.activitytracker.classes.modules.OrgServiceModule;
import com.microsoft.activitytracker.classes.modules.StorageModule;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.squareup.leakcanary.LeakCanary;

public class ActivityTracker extends Application {

    private static BaseComponent baseComponent;
    private static OrgServiceComponent orgServiceComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
        FlowManager.init(new FlowConfig.Builder(this).build());

        baseComponent = DaggerBaseComponent.builder()
                .baseModule(new BaseModule(this))
                .build();
    }

    public static void loginOrgServices(String endpoint, String token) {
        orgServiceComponent = DaggerOrgServiceComponent.builder()
                .orgServiceModule(new OrgServiceModule(endpoint, token))
                .build();
    }

    public static LoginComponent createNewLoginSession(String endpoint) {
        return DaggerLoginComponent.builder()
                .baseComponent(baseComponent)
                .loginModule(new LoginModule(endpoint))
                .build();
    }

    public static StorageComponent openNewStorage() {
        return DaggerStorageComponent.builder()
                .baseComponent(baseComponent)
                .storageModule(new StorageModule())
                .build();
    }

    public static OrgServiceComponent getOrgServiceComponent() {
        return orgServiceComponent;
    }

}