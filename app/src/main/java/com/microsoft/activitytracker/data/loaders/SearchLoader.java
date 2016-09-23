package com.microsoft.activitytracker.data.loaders;

import android.content.Context;

import com.microsoft.activitytracker.classes.ActivityTracker;
import com.microsoft.activitytracker.classes.oDataService;
import com.microsoft.xrm.QueryOptions;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public abstract class SearchLoader extends BaseEntityLoader {

    @Inject oDataService orgService;

    Context context;

    public SearchLoader(Context context, boolean initializeDependency) {
        super(context);

        this.context = context;
        if (initializeDependency) {
            initializeOrgService();
        }
    }

    public void initializeOrgService() {
        ActivityTracker.getOrgServiceComponent().inject(this);
    }

    public void searchFor(String queryString) {
        if (isConnected()) {
            incrementLoadingCount(1);

            searchContacts(queryString);
        }
    }

    private void searchContacts(String query) {
        QueryOptions queryString = new QueryOptions()
                .putSelect("address1_line1", "address1_city", "address1_stateorprovince",
                        "address1_postalcode", "emailaddress1", "telephone1", "customertypecode",
                        "contactid", "fullname", "jobtitle")
                .putLinkedEntity("parentcustomerid")
                .putLinkedEntity("accountid")
                .putFilter("contains(fullname, '" + query + "')")
                .putOrderBy("fullname", true);

        orgService.retrieveMultiple("contacts", queryString)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(Throwable::printStackTrace)
                .doOnNext(retrieveMultipleResponse)
                .subscribe();
    }

}
