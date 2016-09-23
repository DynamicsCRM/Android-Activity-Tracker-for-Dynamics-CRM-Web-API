package com.microsoft.activitytracker.util.binding;

import android.databinding.BindingAdapter;
import android.view.View;
import android.widget.TextView;

import com.microsoft.activitytracker.classes.models.Entity;

public final class CreateBindingAdapters {

    @BindingAdapter("bind:titleSub1")
    public static void setTitleSub1(TextView textView, Entity entity) {
        switch(entity.LogicalName) {
            case "contact":
                if (entity.contains("jobtitle") && !entity.get("jobtitle").equals("")) {
                    textView.setText(entity.get("jobtitle").toString());
                    textView.setVisibility(View.VISIBLE);
                    return;
                }
                break;
        }

        textView.setVisibility(View.GONE);
    }

    @BindingAdapter("bind:titleSub2")
    public static void setTitleSub2(TextView textView, Entity entity) {
        switch(entity.LogicalName) {
            case "contact":
                if (entity.containsRef("parentcustomerid")) {
                    textView.setText(entity.getRef("parentcustomerid").get("FormattedValue").toString());
                    textView.setVisibility(View.VISIBLE);
                    return;
                }
                break;
        }

        textView.setVisibility(View.GONE);
    }




}
