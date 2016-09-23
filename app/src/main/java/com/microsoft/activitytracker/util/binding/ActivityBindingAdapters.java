package com.microsoft.activitytracker.util.binding;

import android.databinding.BindingAdapter;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.microsoft.activitytracker.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public final class ActivityBindingAdapters {

    @BindingAdapter("bind:activityType")
    public static void setActivityType(ImageView imageView, String logicalName) {
        switch (logicalName) {
            case "task":
                imageView.setImageResource(R.drawable.ic_activity_checkin);
                break;
            case "appointment":
                imageView.setImageResource(R.drawable.ic_activity_appointment);
                break;
            case "letter":
                imageView.setImageResource(R.drawable.ic_activity_annotation);
                break;
            case "phonecall":
                imageView.setImageResource(R.drawable.ic_activity_call);
                break;
            default:
                imageView.setImageResource(R.drawable.ic_activity_generic);
                break;
        }
    }

    @BindingAdapter("bind:formatDate")
    public static void setFormattedDate(TextView textView, String date) {
        try {
            SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'", Locale.getDefault());
            SimpleDateFormat toFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            textView.setText(toFormat.format(fromFormat.parse(date).getTime()));
        }
        catch (ParseException exception) {
            Log.e("DateFormatBinding", exception.getMessage());
        }
    }

}
