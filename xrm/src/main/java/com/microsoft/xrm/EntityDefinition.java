package com.microsoft.xrm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class EntityDefinition {

    @JsonProperty("Description")
    private Labels description;

    @JsonProperty("DisplayName")
    private Labels displayName;

    @JsonProperty("LogicalName")
    private String logicalName;

    @JsonProperty("ObjectTypeCode")
    private int objectTypeCode;

    @JsonProperty("PrimaryNameAttribute")
    private String primaryNameAttribute;

    @JsonProperty("PrimaryIdAttribute")
    private String primaryIdAttribute;

    @JsonProperty("SchemaName")
    private String schemaName;

    @JsonProperty("EntityColor")
    private String entityColor;

    @JsonProperty("LogicalCollectionName")
    private String logicalCollectionName;

    @JsonProperty("EntitySetName")
    private String entitySetName;

    public int getObjectTypeCode() {
        return objectTypeCode;
    }

    public Labels getDescription() {
        return description;
    }

    public Labels getDisplayName() {
        return displayName;
    }

    public String getEntityColor() {
        return entityColor;
    }

    public String getEntitySetName() {
        return entitySetName;
    }


    public String getLogicalCollectionName() {
        return logicalCollectionName;
    }


    public String getLogicalName() {
        return logicalName;
    }


    public String getPrimaryIdAttribute() {
        return primaryIdAttribute;
    }


    public String getPrimaryNameAttribute() {
        return primaryNameAttribute;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public static class Builder {

        private Labels description;
        private Labels displayName;
        private String logicalName;
        private int objectTypeCode;
        private String primaryNameAttribute;
        private String primaryIdAttribute;
        private String schemaName;
        private String entityColor;
        private String logicalCollectionName;
        private String entitySetName;


        public Builder setDescription(Labels description) {
            this.description = description;
            return this;
        }

        public Builder setDisplayName(Labels displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder setEntityColor(String entityColor) {
            this.entityColor = entityColor;
            return this;
        }

        public Builder setEntitySetName(String entitySetName) {
            this.entitySetName = entitySetName;
            return this;
        }

        public Builder setLogicalCollectionName(String logicalCollectionName) {
            this.logicalCollectionName = logicalCollectionName;
            return this;
        }

        public Builder setLogicalName(String logicalName) {
            this.logicalName = logicalName;
            return this;
        }

        public Builder setObjectTypeCode(int objectTypeCode) {
            this.objectTypeCode = objectTypeCode;
            return this;
        }

        public Builder setPrimaryIdAttribute(String primaryIdAttribute) {
            this.primaryIdAttribute = primaryIdAttribute;
            return this;
        }

        public Builder setPrimaryNameAttribute(String primaryNameAttribute) {
            this.primaryNameAttribute = primaryNameAttribute;
            return this;
        }

        public Builder setSchemaName(String schemaName) {
            this.schemaName = schemaName;
            return this;
        }

        public EntityDefinition build() {
            EntityDefinition definition = new EntityDefinition();
            definition.description = this.description;
            definition.displayName = this.displayName;
            definition.entityColor = this.entityColor;
            definition.entitySetName = this.entitySetName;
            definition.logicalCollectionName = this.logicalCollectionName;
            definition.logicalName = this.logicalName;
            definition.objectTypeCode = this.objectTypeCode;
            definition.primaryIdAttribute = this.primaryIdAttribute;
            definition.primaryNameAttribute = this.primaryNameAttribute;
            definition.schemaName = this.schemaName;

            return definition;
        }

    }

}
