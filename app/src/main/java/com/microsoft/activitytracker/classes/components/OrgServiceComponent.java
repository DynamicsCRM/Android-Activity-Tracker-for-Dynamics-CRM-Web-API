package com.microsoft.activitytracker.classes.components;

import com.microsoft.activitytracker.classes.modules.OrgServiceModule;
import com.microsoft.activitytracker.classes.oDataService;
import com.microsoft.activitytracker.classes.scopes.OrgScope;
import com.microsoft.activitytracker.data.loaders.DefinitionsLoader;
import com.microsoft.activitytracker.data.loaders.ItemLoader;
import com.microsoft.activitytracker.data.loaders.SearchLoader;
import com.microsoft.activitytracker.ui.CreateActivity;

import dagger.Component;

@OrgScope
@Component(modules = OrgServiceModule.class )
public interface OrgServiceComponent {

    oDataService oDataService();

    void inject(SearchLoader dataManager);
    void inject(ItemLoader itemLoader);
    void inject(CreateActivity createActivity);
    void inject(DefinitionsLoader definitionsLoader);
}
