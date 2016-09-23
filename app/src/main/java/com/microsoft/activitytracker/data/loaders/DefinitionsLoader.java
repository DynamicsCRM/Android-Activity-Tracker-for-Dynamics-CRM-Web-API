package com.microsoft.activitytracker.data.loaders;

import android.content.Context;

import com.microsoft.activitytracker.classes.ActivityTracker;
import com.microsoft.activitytracker.classes.oDataService;
import com.microsoft.activitytracker.data.BaseDataManager;
import com.microsoft.activitytracker.data.storage.DefinitionEntry;
import com.microsoft.xrm.DefinitionMetadataResponse;
import com.microsoft.xrm.QueryOptions;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DefinitionsLoader extends BaseDataManager {

    @Inject
    oDataService orgService;

    public DefinitionsLoader(Context context) {
        super(context);
        ActivityTracker.getOrgServiceComponent().inject(this);
    }

    public void getRequiredMetadataDefinitions() {
        loadStarted();
        incrementLoadingCount();

        QueryOptions query = new QueryOptions()
                .putFilter("LogicalName eq 'contact' or IsActivity eq true")
                .putSelect("Description","DisplayName","LogicalName","ObjectTypeCode",
                        "PrimaryNameAttribute","PrimaryIdAttribute","SchemaName","EntityColor",
                        "LogicalCollectionName","EntitySetName");

        orgService.getEntityDefinitions(query)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError(throwable -> {
                decrementLoadingCount();
                loadFailed(throwable.getMessage());
            })
            .doOnNext(response -> {
                if (response.isSuccessful() && response.body() != null) {
                    DefinitionMetadataResponse definitions = response.body();

                    DefinitionEntry.addEntityDefinitions(definitions.getDefinitions());

                    decrementLoadingCount();
                    loadFinished();
                }
                else {
                    try {
                        Observable.error(new Exception(response.errorBody().string()));
                    }
                    catch(Exception ex) {
                        Observable.error(ex);
                    }
                }
            })
            .subscribe();
    }
}
