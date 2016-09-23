package com.microsoft.activitytracker.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.design.widget.Snackbar;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.microsoft.activitytracker.BR;
import com.microsoft.activitytracker.R;
import com.microsoft.activitytracker.classes.ActivityTracker;
import com.microsoft.activitytracker.classes.models.Entity;
import com.microsoft.activitytracker.classes.models.Enums;
import com.microsoft.activitytracker.classes.oDataService;
import com.microsoft.activitytracker.data.storage.HistoryEntry;
import com.microsoft.activitytracker.databinding.ActivityCreateBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

public final class CreateActivity extends AppCompatActivity implements View.OnClickListener {

    @Inject
    oDataService orgService;

    @Enums.CreateTypes
    private String createType;

    private Entity parent;

    private Calendar date = Calendar.getInstance();
    private ProgressDialog progressDialog;

    private Resources resources;
    private ActivityCreateBinding binding;
    private ActionBar actionbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create);

        ActivityTracker.getOrgServiceComponent().inject(this);
        resources = getResources();

        setSupportActionBar(binding.appbar.toolbar);
        actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setDisplayShowTitleEnabled(false);
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(resources.getString(R.string.creating_activity));

        binding.dateLayout.dateSection.setOnClickListener(this);
        buildPage();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.date_layout:
                new DatePickerDialog(CreateActivity.this, dateSetListener,
                    date.get(Calendar.YEAR),
                    date.get(Calendar.MONTH),
                    date.get(Calendar.DAY_OF_MONTH)).show();
                break;
        }
    }

    @ColorInt
    private int getColorOld(int resourceId) {
        return resources.getColor(resourceId);
    }

    @ColorInt
    @TargetApi(Build.VERSION_CODES.M)
    private int getColorM(int resourceId) {
        return resources.getColor(resourceId, null);
    }

    private void buildPage() {
        Intent intent = getIntent();
        if (intent.hasExtra(ItemActivity.CREATE_TYPE)) {
            createType = Enums.getCreateType(intent.getStringExtra(ItemActivity.CREATE_TYPE));
        }
        if (intent.hasExtra(ItemActivity.CURRENT_ID)) {
            parent = HistoryEntry.getEntry(intent.getStringExtra(ItemActivity.CURRENT_ID));
        }

        binding.setVariable(BR.parent, parent);
        binding.executePendingBindings();

        Resources resources = getResources();
        switch(parent.LogicalName) {
            case "contact":
                int color = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        getColorM(R.color.contact_color) : getColorOld(R.color.contact_color);
                binding.titleLayout.itemTitle.setTextColor(color);
                break;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        switch(createType) {
            case Enums.CHECKIN:
                actionbar.setTitle("Check In");
                String subject = resources.getString(R.string.check_in_subject);
                String date = dateFormat.format(this.date.getTime());

                binding.subjectLayout.subject.setText(String.format(subject, parent.getName(), date));
                binding.dateLayout.selectedDate.setText(date);
                break;
        }
    }

    private void validateForm() {
        if (binding.subjectLayout.subject.getText().toString().equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.subject_required);

            builder.setPositiveButton(R.string.ok, (dialog, id) -> {

            });

            AlertDialog dialog = builder.create();
            dialog.setOnDismissListener(dialog1 ->
                    findViewById(R.id.submit_activity).setEnabled(true));

            dialog.show();
            return;
        }

        createActivity();
    }

    private void displayErrorSnackbar(String error) {
        Snackbar
            .make(binding.coordinatedContainer, error, Snackbar.LENGTH_LONG)
            .show();
    }

    private void createActivity() {
        progressDialog.show();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'", Locale.getDefault());
        String date = dateFormat.format(this.date.getTime());

        Map<String, Object> entity = new ArrayMap<>();
        entity.put("subject", binding.subjectLayout.subject.getText().toString());
        entity.put("scheduledend", date);
        entity.put("actualend", date);
        entity.put("description", binding.notesLayout.notes.getText().toString());
        entity.put("statecode", 1);
        entity.put("regardingobjectid_contact_task@odata.bind", "/" +
                parent.getLogicalCollectionName() + "(" + parent.getId() + ")");

        orgService.create("tasks", entity)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError(throwable -> createError.call())
            .doOnNext(response -> {
                if (response.isSuccessful()) {
                    switch(createType) {
                        case Enums.CHECKIN:
                            finishActivity.call();
                            return;
                    }
                }

                createError.call();
            }).subscribe();
    }

    private Action0 createError = () -> {
        progressDialog.setOnDismissListener(dialog -> {
            displayErrorSnackbar("Unable to create new activity");
        });

        progressDialog.dismiss();
    };

    private Action0 finishActivity = () -> {
        progressDialog.setOnDismissListener(dialog -> {
            setResult(RESULT_OK);
            finish();
        });
        progressDialog.dismiss();
    };

    private void SetDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        EditText subject = binding.subjectLayout.subject;

        String fullName = parent.get("fullname").toString();
        String currentSubject = subject.getText().toString();
        String formattedDate = dateFormat.format(date.getTime());

        if (currentSubject.contains(String.format(getString(R.string.check_in_subject), fullName, ""))) {
            subject.setText(String.format(getString(R.string.check_in_subject),
                    fullName, formattedDate));
        }

        binding.dateLayout.selectedDate.setText(formattedDate);
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, monthOfYear);
        date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        SetDate();
    };

    @Override
    public void onBackPressed() {
        if (!progressDialog.isShowing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.warning_error);

            builder.setPositiveButton(R.string.yes, (dialog, id) -> {
                setResult(RESULT_CANCELED);
                finish();
            });
            builder.setNegativeButton(R.string.cancel, (dialog, id) -> {
                //Don't do anything, ignore the back press
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_check_in, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.submit_activity:
                findViewById(R.id.submit_activity).setEnabled(false);
                validateForm();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
