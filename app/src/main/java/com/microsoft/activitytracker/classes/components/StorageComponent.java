package com.microsoft.activitytracker.classes.components;

import com.microsoft.activitytracker.classes.scopes.StorageScope;
import com.microsoft.activitytracker.classes.modules.StorageModule;
import com.microsoft.activitytracker.ui.MainActivity;
import com.microsoft.activitytracker.ui.SetupActivity;

import dagger.Component;

@StorageScope
@Component(dependencies = BaseComponent.class, modules = StorageModule.class)
public interface StorageComponent {

    void inject(MainActivity mainActivity);
    void inject(SetupActivity setupActivity);

}
