package com.microsoft.activitytracker.classes.components;

import com.microsoft.activitytracker.classes.modules.LoginModule;
import com.microsoft.activitytracker.classes.scopes.LoginScope;

import dagger.Component;

@LoginScope
@Component(dependencies = BaseComponent.class, modules = LoginModule.class)
public interface LoginComponent {

    LoginModule.AuthorityService getEndpoint();

}
