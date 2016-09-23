package com.microsoft.activitytracker.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.microsoft.activitytracker.R;
import com.microsoft.activitytracker.classes.models.Entity;
import com.microsoft.activitytracker.data.DataLoadingSubject;
import com.microsoft.activitytracker.data.loaders.ItemLoader;
import com.microsoft.activitytracker.data.storage.HistoryEntry;
import com.microsoft.activitytracker.ui.adapters.ActivitiesAdapter;
import com.microsoft.activitytracker.ui.widgets.FloatingActionMenu;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public final class ItemActivity extends AppCompatActivity implements DataLoadingSubject.DataLoadingCallbacks {

    private final int CREATE_ACTIVITY = 1;

    public static final String CURRENT_ID = "entityId";
    static final String CREATE_TYPE = "createType";

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.activity_list) RecyclerView activityList;
    @Bind(R.id.item_title) TextView itemTitle;
    @Bind(R.id.coordinated_container) CoordinatorLayout baseView;
    @Bind(R.id.action_menu) FloatingActionMenu fabMenu;
    @Bind(R.id.menu_main_button) FloatingActionButton fab;
    @Bind(R.id.loading_progress) ProgressBar loading;
    @Bind(R.id.sub1) TextView sub1;
    @Bind(R.id.sub2) TextView sub2;

    @Bind(R.id.phone) TextView phone;
    @Bind(R.id.phone_wrapper) View phoneContainer;

    @Bind(R.id.address) TextView address;
    @Bind(R.id.address_wrapper) View addressContainer;

    @Bind(R.id.email) TextView email;
    @Bind(R.id.mail_wrapper) View emailContainer;

    private ItemLoader loader;
    private ActivitiesAdapter activitiesAdapter;
    private PackageManager packageManager;
    private Entity entity;

    private CompositeSubscription subscriptions;
    private PublishSubject<Entity> entityChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        ButterKnife.bind(this);

        packageManager = this.getPackageManager();
        subscriptions = new CompositeSubscription();
        entityChanged = PublishSubject.create();

        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setDisplayShowTitleEnabled(false);
        }

        activitiesAdapter = new ActivitiesAdapter(this);
        activityList.setLayoutManager(new LinearLayoutManager(this));
        activityList.setItemAnimator(new DefaultItemAnimator());

        loader = new ItemLoader(this, true) {
            @Override
            public void onEntityChanged(Entity updatedEntity) {
                entityChanged.onNext(updatedEntity);
            }

            @Override
            public void onDataLoaded(List<Entity> retrieveResponse) {
                activitiesAdapter.updateCollection(retrieveResponse);

                if (activityList.getAdapter() == null) {
                    activityList.setAdapter(activitiesAdapter);
                }
            }
        };

        loader.registerCallback(this);
        subscriptions.add(entityChanged.doOnNext(updateEntity).subscribe());

        Intent intent = getIntent();
        if (intent.hasExtra(CURRENT_ID)) {
            entityChanged.onNext(HistoryEntry.getEntry(intent.getStringExtra(CURRENT_ID)));
            loader.loadRecentActivites(entity.getLogicalCollectionName(), entity.getId());
            loader.checkIfUpToDate(entity);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscriptions.unsubscribe();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.address_wrapper)
    void openMaps() {
        if (address.getVisibility() != View.GONE && !address.getText().equals("")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    String.format("geo:0,0?q=%s", address.getText())
            ));

            List<ResolveInfo> infos = packageManager.queryIntentActivities(intent, 0);

            if (infos.size() > 0) {
                startActivity(intent);
            }
            else {
                dataFailedLoading("No application available to handle this action");
            }
        }
    }

    @OnClick(R.id.phone_wrapper)
    void openDialer() {
        if (entity.contains("telephone1") && !entity.get("telephone1").equals("")) {
            Intent dialer = new Intent(Intent.ACTION_DIAL);
            dialer.setData(Uri.parse(String.format("tel:%s", entity.get("telephone1"))));

            List<ResolveInfo> infos = packageManager.queryIntentActivities(dialer, 0);

            if (infos.size() > 0) {
                startActivity(dialer);
            }
            else {
                dataFailedLoading("No application available to handle this action");
            }
        }
    }

    @OnClick(R.id.mail_wrapper)
    void openEmail() {
        if (entity.contains("emailaddress1") && !entity.get("emailaddress1").equals("")) {
            Intent send = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",
                    entity.get("emailaddress1").toString(), null));

            List<ResolveInfo> infos = packageManager.queryIntentActivities(send, 0);

            if (infos.size() > 0) {
                startActivity(Intent.createChooser(send, "Send Email Using"));
            }
            else {
                dataFailedLoading("No application available to handle this action");
            }
        }
    }

    private boolean isConnected() {
        ConnectivityManager manager = (ConnectivityManager)this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @OnClick({R.id.add_checkin, R.id.add_annotation, R.id.add_followup, R.id.add_phonecall})
    void createCheckIn(View view) {
        if (!isConnected()) {
            Snackbar.make(baseView, getString(R.string.network_error),
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(ItemActivity.this, CreateActivity.class);

        switch(view.getId()) {
            case R.id.add_checkin:
                intent.putExtra(CREATE_TYPE, "checkin");
                break;
        }

        if (intent.hasExtra(CREATE_TYPE)) {
            fabMenu.close();
            intent.putExtra(CURRENT_ID, entity.getId().toString());

            startActivityForResult(intent, CREATE_ACTIVITY);
        }
    }

    private Action1<Entity> updateEntity = newEntity -> {
        this.entity = newEntity;
        itemTitle.setText(entity.getName());
        populateEntity();
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CREATE_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    loader.loadRecentActivites(entity.getLogicalCollectionName(), entity.getId());
                }
                break;
        }
    }

    private void hidePhoneCall() {
        FloatingActionMenu actionMenu = (FloatingActionMenu)findViewById(R.id.action_menu);
        actionMenu.removeViews(6, 2);
    }

    private void setEntityTitleColor(int color) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            itemTitle.setTextColor(
                    getResources().getColor(color));
        }
        else {
            itemTitle.setTextColor(
                    getResources().getColor(color, null));
        }
    }

    private void populateEntity() {
        switch(entity.LogicalName) {
            case "contact":
                populateContact();
                break;
        }
    }

    private void populateContact() {
        setEntityTitleColor(R.color.contact_color);

        if (entity.contains("jobtitle") && !entity.get("jobtitle").equals("")) {
            sub1.setText(entity.get("jobtitle").toString());
            sub1.setVisibility(View.VISIBLE);
        }
        if (entity.containsRef("parentcustomerid")) {
            sub2.setText(entity.getRef("parentcustomerid").get("FormattedValue").toString());
            sub2.setVisibility(View.VISIBLE);
        }


        if (entity.contains("telephone1") && !entity.get("telephone1").equals("")) {
            phone.setText(entity.get("telephone1").toString());
            phoneContainer.setVisibility(View.VISIBLE);
        }
        else {
            hidePhoneCall();
        }

        if (entity.contains("emailaddress1") && !entity.get("emailaddress1").equals("")) {
            email.setText(entity.get("emailaddress1").toString());
            emailContainer.setVisibility(View.VISIBLE);
        }

        String addressValue;
        if (entity.contains("address1_city")) {
            addressValue = String.format("%s\n%s, %s %s",
                entity.contains("address1_line1") ? entity.get("address1_line1") : "",
                entity.get("address1_city"),
                entity.contains("address1_stateorprovince") ? entity.get("address1_stateorprovince") : "",
                entity.contains("address1_postalcode") ? entity.get("address1_postalcode") : "");
        }
        else {
            addressValue = String.format("%s\n%s %s",
                entity.contains("address1_line1") ? entity.get("address1_line1") : "",
                entity.contains("address1_stateorprovince") ? entity.get("address1_stateorprovince") : "",
                entity.contains("address1_postalcode") ? entity.get("address1_postalcode") : "");
        }

        if (addressValue != null && !addressValue.equals("")) {
            address.setText(addressValue);
            addressContainer.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void dataStartedLoading() {
        loading.setVisibility(View.VISIBLE);
    }

    @Override
    public void dataFinishedLoading() {
        loading.setVisibility(View.GONE);
    }

    @Override
    public void dataFailedLoading(String errorMessage) {
        Snackbar.make(baseView, errorMessage, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.finishAfterTransition();
        }
        else {
            super.onBackPressed();
        }
    }
}
