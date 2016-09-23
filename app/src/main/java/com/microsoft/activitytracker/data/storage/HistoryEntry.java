package com.microsoft.activitytracker.data.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.activitytracker.classes.models.Entity;
import com.microsoft.activitytracker.data.TrackerDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Table(name = "History", database = TrackerDatabase.class)
public final class HistoryEntry extends BaseModel {

    @PrimaryKey
    String id;

    @Column
    String eTag;

    @Column
    String logicalName;

    @Column
    Date lastViewed;

    @Column
    Blob attributes;

    @Column
    Blob formattedValues;

    @Column
    Blob references;

    @Column
    String primaryName;


    public static void addNewEntry(Entity entity) {
        HistoryEntry entry = new HistoryEntry();

        entry.id = entity.getId().toString();
        entry.logicalName = entity.LogicalName;
        entry.lastViewed = new Date();
        entry.primaryName = entity.getPrimaryNameAttribute();
        entry.eTag = entity.getETag();

        try {
            ObjectMapper mapper = new ObjectMapper();

            entry.attributes = new Blob(mapper.writeValueAsBytes(entity.getAttributes()));
            entry.formattedValues = new Blob(mapper.writeValueAsBytes(entity.getFormattedValues()));
            entry.references = new Blob(mapper.writeValueAsBytes(entity.getReferences()));
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }

        entry.save();
    }

    public static List<Entity> getHistoryEntries() {
        List<HistoryEntry> entries = new Select()
            .from(HistoryEntry.class)
            .orderBy(OrderBy.fromProperty(HistoryEntry_Table.lastViewed).descending())
            .queryList();

        int size = entries.size();
        List<Entity> entities = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            entities.add(buildEntity(entries.get(i)));
        }

        return entities;
    }

    private static Entity buildEntity(HistoryEntry entry) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<Map<String, Object>> mapRef = new TypeReference<Map<String, Object>>() {};

            return new Entity.Builder()
                .setId(entry.id)
                .setLogicalName(entry.logicalName)
                .setETag(entry.eTag)
                .setEntityDefinition(DefinitionEntry.getDefinitionFromLogicalName(entry.logicalName))
                .setAttributes(mapper.readValue(entry.attributes.getBlob(), mapRef))
                .setFormattedValues(mapper.readValue(entry.formattedValues.getBlob(), mapRef))
                .setReferences(mapper.readValue(entry.references.getBlob(),
                        new TypeReference<Map<String, Map<String, Object>>>() {}))
                .build();
        }
        catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void clearRecentRecords() {
        List<HistoryEntry> entries = new Select()
                .from(HistoryEntry.class)
                .queryList();

        FlowManager.getDatabase(TrackerDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction
                    .Builder<HistoryEntry>(BaseModel::delete)
                    .addAll(entries)
                    .build())
                .build()
                .execute();
    }

    public static Entity getEntry(String id) {
        HistoryEntry entry = new Select()
            .from(HistoryEntry.class)
            .where(HistoryEntry_Table.id.eq(id))
            .querySingle();

        return buildEntity(entry);
    }

}