package com.microsoft.activitytracker.ui.adapters;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.microsoft.activitytracker.R;
import com.microsoft.activitytracker.BR;
import com.microsoft.activitytracker.classes.models.Entity;
import com.microsoft.activitytracker.data.storage.HistoryEntry;
import com.microsoft.activitytracker.ui.ItemActivity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


import butterknife.Bind;
import butterknife.ButterKnife;

public final class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int ENTITY_VIEW_TYPE = 0;

    private Activity host;
    private boolean toClear;
    private List<Entity> items = new ArrayList<>();

    // Usually bad practice to pass activities, but this is required for shared element
    // animations when we open a new activity
    public MainAdapter(Activity host) {
        this.host = host;
    }

    public static class EntityHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.primary_name_attribute) TextView primaryName;
        @Bind(R.id.subtitle) TextView subTitle;

        ViewDataBinding binding;

        public EntityHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch(viewType) {
            case ENTITY_VIEW_TYPE:
                return createEntityHolder(parent);
        }

        return null;
    }

    private EntityHolder createEntityHolder(ViewGroup parent) {
        final EntityHolder holder = new EntityHolder(LayoutInflater.from(host).inflate(
                R.layout.entity_item_layout, parent, false));

        holder.itemView.setOnClickListener(v -> {
            Entity selectedEntity = (Entity) getItem(holder.getAdapterPosition());
            HistoryEntry.addNewEntry(selectedEntity);

            Intent itemIntent = new Intent(host, ItemActivity.class);
            itemIntent.putExtra(ItemActivity.CURRENT_ID, selectedEntity.getId().toString());

            openActivity(itemIntent, holder);
        });

        return holder;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void openActivity(Intent intent, EntityHolder holder) {
        ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(host,
            new Pair<>(holder.primaryName, host.getString(R.string.primary_name_transition)),
            new Pair<>(holder.subTitle, host.getString(R.string.subtitle_transition)));

        ActivityCompat.startActivity(host, intent, activityOptions.toBundle());
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch(getItemViewType(position)) {
            case ENTITY_VIEW_TYPE:
                bindEntity((Entity) getItem(position), (EntityHolder) holder);
                break;
        }
    }

    private void bindEntity(Entity entity, EntityHolder holder) {
        holder.binding.setVariable(BR.entity, entity);
        holder.binding.executePendingBindings();
    }

    public class TitleComparator implements Comparator<Entity> {
        @Override
        public int compare(Entity entity1, Entity entity2) {
            return entity1.getName().compareTo(entity2.getName());
        }
    }

    public void updateCollection(List<Entity> newChildren) {
        int newSize;
        int oldSize = items.size();

        if (toClear) {
            toClear = false;
            newSize = newChildren.size();
            items = newChildren;
        }
        else {
            newSize = oldSize + newChildren.size();
            items.addAll(newChildren);
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

    public void setToClear(boolean setTo) {
        toClear = setTo;
    }

    public void clear() {
        int oldSize = items.size();

        items.clear();
        notifyItemRangeRemoved(0, oldSize);
    }

    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return ENTITY_VIEW_TYPE;
    }
}
