package com.microsoft.activitytracker.ui.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.microsoft.activitytracker.BR;
import com.microsoft.activitytracker.R;
import com.microsoft.activitytracker.classes.models.Entity;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public final class ActivitiesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int HEADER_VIEW = 0;
    private final int ACTIVITY_VIEW = 1;

    private Context mContext;
    private boolean toClear;

    private LayoutInflater layoutInflater;
    private List<Entity> items;

    public static class ActivityHolder extends RecyclerView.ViewHolder {

        ViewDataBinding binding;

        public ActivityHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }

    public static class HeaderHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.message) TextView text;

        public HeaderHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    public ActivitiesAdapter(Context context) {
        items = new ArrayList<>();

        this.layoutInflater = LayoutInflater.from(context);
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch(viewType) {
            case HEADER_VIEW:
                return createHeaderViewHolder(parent);
            default:
                return createActivityViewHolder(parent);
        }
    }

    private HeaderHolder createHeaderViewHolder(ViewGroup parent) {
        final HeaderHolder holder = new HeaderHolder(layoutInflater.inflate(R.layout.list_header,
                parent, false));

        holder.itemView.setOnClickListener(v -> {

        });

        return holder;
    }

    private ActivityHolder createActivityViewHolder(ViewGroup parent) {
        final ActivityHolder holder = new ActivityHolder(layoutInflater.inflate(R.layout.activity_list_item,
                parent, false));

        holder.itemView.setOnClickListener(v -> {

        });

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch(getItemViewType(position)) {
            case HEADER_VIEW:
                String header = holder.itemView.getContext()
                        .getString(R.string.no_complete_activities);
                bindHeaderView((HeaderHolder)holder, header);
                break;
            case ACTIVITY_VIEW:
                bindActivityView((ActivityHolder) holder, (Entity) getItem(position));
                break;
        }
    }

    private void bindHeaderView(HeaderHolder holder, String message) {

        holder.text.setText(message);

    }

    private void bindActivityView(ActivityHolder holder, Entity item) {
        holder.binding.setVariable(BR.activity, item);
        holder.binding.executePendingBindings();
    }


    public void updateCollection(List<Entity> newChildren) {
        int newSize = newChildren.size();
        int oldSize = items.size();

        items = newChildren;

        if (oldSize == 0) {
            notifyItemRangeRemoved(0, 1);
            notifyItemRangeChanged(0, 1);
        }

        if (newSize < oldSize) {
            notifyItemRangeRemoved(newSize, oldSize - newSize);
            notifyItemRangeChanged(0, newSize);
        }
        if (newSize == oldSize) {
            notifyItemRangeChanged(0, newSize);
        }
        if (newSize > oldSize) {
            notifyItemRangeChanged(0, oldSize);
            notifyItemRangeInserted(oldSize, newSize - oldSize);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.size() == 0 ? HEADER_VIEW : ACTIVITY_VIEW;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return items.size() == 0 ? 1 : items.size();
    }

    public Object getItem(int position) {
        return items.size() != 0 ? items.get(position) : null;
    }

    public void clear() {
        int size = items.size();

        items.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void setToClear(boolean toClear) {
        this.toClear = toClear;
    }

//    public class ActualEndComparator implements Comparator<Entity> {
//        @Override
//        public int compare(Entity entity1, Entity entity2) {
//            return -(EntityUtils.getDate(entity1).compareTo(EntityUtils.getDate(entity2)));
//        }
//    }
}
