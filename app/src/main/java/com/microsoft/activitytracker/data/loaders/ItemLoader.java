package com.microsoft.activitytracker.data.loaders;

import android.content.Context;

import com.microsoft.activitytracker.R;
import com.microsoft.activitytracker.classes.ActivityTracker;
import com.microsoft.activitytracker.classes.models.Entity;
import com.microsoft.activitytracker.classes.oDataService;
import com.microsoft.activitytracker.data.storage.DefinitionEntry;
import com.microsoft.activitytracker.data.storage.HistoryEntry;
import com.microsoft.xrm.EntityDefinition;
import com.microsoft.xrm.QueryOptions;

import java.util.UUID;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public abstract class ItemLoader extends BaseEntityLoader {

    @Inject oDataService orgService;

    Context context;

    public abstract void onEntityChanged(Entity updatedEntity);

    public ItemLoader(Context context, boolean initializeDependency) {
        super(context);

        this.context = context;
        if (initializeDependency && ActivityTracker.getOrgServiceComponent() != null) {
            initializeOrgService();
        }
    }

    public void initializeOrgService() {
        ActivityTracker.getOrgServiceComponent().inject(this);
    }

    public void checkIfUpToDate(Entity entity) {
        if (isConnected()) {
            if (orgService != null) {
                loadFailed(context.getString(R.string.orgservice_error));
                return;
            }

            QueryOptions query = new QueryOptions();
            switch(entity.LogicalName) {
                case "contact":
                    query.putSelect("address1_line1","address1_city","address1_stateorprovince",
                            "address1_postalcode","emailaddress1","telephone1","customertypecode",
                            "contactid","fullname","jobtitle")
                        .putLinkedEntity("parentcustomerid")
                        .putLinkedEntity("accountid");
                    break;
            }

            orgService.hasChanged(entity.getLogicalCollectionName(), entity.getId(), query,
                    entity.getETag())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> loadFailed(throwable.getMessage()))
                .doOnNext(response -> {
                    if (response.isSuccessful()) {
                        if (response.code() != 304) {
                            EntityDefinition definition = DefinitionEntry.getDefinitionFromLogicalName(
                                    entity.LogicalName);

                            Entity updated = new Entity.Builder()
                                    .setLogicalName(definition.getLogicalName())
                                    .setEntitySuper(response.body())
                                    .setEntityDefinition(definition)
                                    .build();

                            HistoryEntry.addNewEntry(updated);
                            onEntityChanged(updated);
                        }
                    }
                }).subscribe();
        }
    }

    public void loadRecentActivites(final String collectionLogicalName, final UUID id) {
        if (isConnected()) {
            loadStarted();
            incrementLoadingCount();

            QueryOptions query = new QueryOptions()
                    .putSelect("activityid","subject","actualend","description")
                    .putFilter("actualend ne null")
                    .putOrderBy("actualend", false);

            orgService.retrieveRelationship(collectionLogicalName, id, "Contact_ActivityPointers", query)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(throwable -> loadFailed(throwable.getMessage()))
                    .doOnNext(retrieveMultipleResponse)
                    .subscribe();

        }
    }
}
