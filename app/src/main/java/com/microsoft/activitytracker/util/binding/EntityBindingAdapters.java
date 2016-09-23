package com.microsoft.activitytracker.util.binding;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.databinding.BindingAdapter;
import android.os.Build;
import android.widget.ImageView;
import android.widget.TextView;

import com.microsoft.activitytracker.R;
import com.microsoft.activitytracker.classes.models.Entity;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

public final class EntityBindingAdapters {

    @BindingAdapter("bind:entityType")
    public static void setEntityType(ImageView imageView, String logicalName) {
        switch (logicalName) {
            case "account":
                imageView.setBackgroundResource(R.drawable.account_circle_background);
                imageView.setImageResource(R.drawable.ic_account);
                break;
            case "contact":
                imageView.setBackgroundResource(R.drawable.contact_circle_background);
                imageView.setImageResource(R.drawable.ic_contact);
                break;
            case "lead":

                break;
            case "opportunity":
                imageView.setBackgroundResource(R.drawable.opportunity_circle_background);
                imageView.setImageResource(R.drawable.ic_opportunity);
                break;
        }
    }

    @BindingAdapter("bind:entityType")
    public static void setEntityType(TextView textView, String logicalName) {
        Resources resources = textView.getContext().getResources();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setEntityTypeAndroidM(textView, logicalName, resources);
        }
        else {
            setEntityTypeAndroidL(textView, logicalName, resources);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    static void setEntityTypeAndroidM(TextView textView, String logicalName, Resources resources) {
        switch(logicalName) {
            case "account":
                textView.setTextColor(resources.getColor(R.color.account_color, null));
                break;
            case "contact":
                textView.setTextColor(resources.getColor(R.color.contact_color, null));
                break;
            case "opportunity":
                textView.setTextColor(resources.getColor(R.color.opportunity_color, null));
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    static void setEntityTypeAndroidL(TextView textView, String logicalName, Resources resources) {
        switch(logicalName) {
            case "account":
                textView.setTextColor(resources.getColor(R.color.account_color));
                break;
            case "contact":
                textView.setTextColor(resources.getColor(R.color.contact_color));
                break;
            case "lead":

                break;
            case "opportunity":
                textView.setTextColor(resources.getColor(R.color.opportunity_color));
                break;
        }
    }

    @BindingAdapter("bind:entitySubTitle")
    public static void setEntitySubTitle(TextView textView, Entity entity) {
        switch(entity.LogicalName) {
            case "contact":
                contactSubtitle(textView, entity);
                break;
        }
    }

    static void contactSubtitle(TextView textView, Entity entity) {
        ArrayList<String> jobAccount = new ArrayList<>();

        if (entity.contains("jobtitle") && !entity.get("jobtitle").equals("")) {
            jobAccount.add(entity.get("jobtitle").toString());
        }
        if (entity.containsRef("parentcustomerid")) {
            jobAccount.add(entity.getRef("parentcustomerid").get("FormattedValue").toString());
        }

        textView.setText(StringUtils.join(jobAccount.toArray(), ", "));
    }
}
