package com.microsoft.activitytracker.classes.components;

import android.app.Application;
import android.content.SharedPreferences;

import com.microsoft.activitytracker.classes.modules.BaseModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = { BaseModule.class })
public interface BaseComponent {

    Application application();
    SharedPreferences sharedPreferences();

}
