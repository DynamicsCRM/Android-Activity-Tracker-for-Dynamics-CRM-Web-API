package com.microsoft.activitytracker.data.storage;

import android.support.v4.util.ArrayMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.activitytracker.data.TrackerDatabase;
import com.microsoft.xrm.EntityDefinition;
import com.microsoft.xrm.Labels;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Table(name = "Definitions", database = TrackerDatabase.class)
public final class DefinitionEntry extends BaseModel {

    @PrimaryKey
    int objectTypeCode;
    @Column
    Blob description;
    @Column
    Blob displayName;
    @Column
    String logicalName;
    @Column
    String primaryNameAttribute;
    @Column
    String primaryIdAttribute;
    @Column
    String schemaName;
    @Column
    String entityColor;
    @Column
    String logicalCollectionName;
    @Column
    String entitySetName;

    public static void addEntityDefinitions(List<EntityDefinition> definitions) {
        int size = definitions.size();
        List<DefinitionEntry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            EntityDefinition definition = definitions.get(i);
            DefinitionEntry entry = new DefinitionEntry();

            entry.objectTypeCode = definition.getObjectTypeCode();
            entry.logicalName = definition.getLogicalName();
            entry.primaryNameAttribute = definition.getPrimaryNameAttribute();
            entry.primaryIdAttribute = definition.getPrimaryIdAttribute();
            entry.schemaName = definition.getSchemaName();
            entry.entityColor = definition.getEntityColor();
            entry.logicalCollectionName = definition.getLogicalCollectionName();
            entry.entitySetName = definition.getEntitySetName();

            try {
                ObjectMapper mapper = new ObjectMapper();

                entry.description = new Blob(mapper.writeValueAsBytes(definition.getDescription()));
                entry.displayName = new Blob(mapper.writeValueAsBytes(definition.getDisplayName()));
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }

            entries.add(entry);
        }

        FlowManager.getDatabase(TrackerDatabase.class)
                .beginTransactionAsync(new ProcessModelTransaction
                        .Builder<DefinitionEntry>(BaseModel::save)
                        .addAll(entries)
                        .build())
                .build()
                .execute();
    }

    private static EntityDefinition buildDefinition(DefinitionEntry entry) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            return new EntityDefinition.Builder()
                .setDescription(mapper.readValue(entry.description.getBlob(), Labels.class))
                .setDisplayName(mapper.readValue(entry.displayName.getBlob(), Labels.class))
                .setEntityColor(entry.entityColor)
                .setEntitySetName(entry.entitySetName)
                .setLogicalCollectionName(entry.logicalCollectionName)
                .setLogicalName(entry.logicalName)
                .setObjectTypeCode(entry.objectTypeCode)
                .setPrimaryIdAttribute(entry.primaryIdAttribute)
                .setPrimaryNameAttribute(entry.primaryNameAttribute)
                .setSchemaName(entry.schemaName)
                .build();
        }
        catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static EntityDefinition getDefinition(String collectionLogicalName) {
        DefinitionEntry entry = new Select()
            .from(DefinitionEntry.class)
            .where(DefinitionEntry_Table.logicalCollectionName.eq(collectionLogicalName))
            .querySingle();

        return buildDefinition(entry);
    }

    public static Map<String, EntityDefinition> getDefinitions() {
        List<DefinitionEntry> entries = new Select()
                .from(DefinitionEntry.class)
                .queryList();

        int size = entries.size();
        Map<String, EntityDefinition> definitions = new ArrayMap<>(size);
        for (int i = 0; i < size; i++) {
            EntityDefinition definition = buildDefinition(entries.get(i));
            if (definition != null) {
                definitions.put(definition.getLogicalName(), definition);
            }
        }

        return definitions;
    }

    public static EntityDefinition getDefinitionFromLogicalName(String logicalName) {
        DefinitionEntry entry = new Select()
                .from(DefinitionEntry.class)
                .where(DefinitionEntry_Table.logicalName.eq(logicalName))
                .querySingle();

        return buildDefinition(entry);
    }

}