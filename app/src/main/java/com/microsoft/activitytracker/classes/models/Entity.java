package com.microsoft.activitytracker.classes.models;

import com.microsoft.xrm.EntityDefinition;

import java.util.Map;
import java.util.UUID;

public class Entity extends com.microsoft.xrm.Entity {

    public final String LogicalName;

    private String collectionLogicalName;
    private String primaryNameAttribute;
    private String primaryIdAttribute;
    private UUID id;

    public Entity(String logicalName) {
        this.LogicalName = logicalName;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return attributes.get(primaryNameAttribute).toString();
    }

    public String getLogicalCollectionName() {
        return this.collectionLogicalName;
    }

    public String getPrimaryIdAttribute() {
        return primaryIdAttribute;
    }

    public String getPrimaryNameAttribute() {
        return primaryNameAttribute;
    }

    public static class Builder {

        EntityDefinition definition;
        String eTag;

        String logicalName;
        String id;

        Map<String, Object> attributes;
        Map<String, Object> formattedValues;
        Map<String, Map<String, Object>> references;

        public Builder setETag(String eTag) {
            this.eTag = eTag;
            return this;
        }

        public Builder setLogicalName(String logicalName) {
            this.logicalName = logicalName;
            return this;
        }

        public Builder setAttributes(Map<String, Object> attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder setFormattedValues(Map<String, Object> formattedValues) {
            this.formattedValues = formattedValues;
            return this;
        }

        public Builder setReferences(Map<String, Map<String, Object>> references) {
            this.references = references;
            return this;
        }

        public Builder setEntityDefinition(EntityDefinition definition) {
            this.definition = definition;
            return this;
        }

        public Builder setId(String attribute) {
            this.id = attribute;
            return this;
        }

        public Builder setEntitySuper(com.microsoft.xrm.Entity entity) {
            this.references = entity.getReferences();
            this.formattedValues = entity.getFormattedValues();
            this.attributes = entity.getAttributes();
            this.eTag = entity.getETag();
            return this;
        }

        public Entity build() {
            Entity entity = new Entity(logicalName);
            entity.attributes = this.attributes;
            entity.formattedValues = this.formattedValues;
            entity.references = this.references;

            entity.eTag = this.eTag;
            entity.collectionLogicalName = this.definition.getLogicalCollectionName();
            entity.primaryNameAttribute = definition.getPrimaryNameAttribute();
            entity.primaryIdAttribute = definition.getPrimaryIdAttribute();

            if (this.id != null) {
                entity.id = UUID.fromString(this.id);
            }
            else {
                entity.id = UUID.fromString(attributes
                        .get(definition.getPrimaryIdAttribute()).toString());
            }

            return entity;
        }

    }

}
