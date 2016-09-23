package com.microsoft.xrm;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Entity extends BaseResponse {

    @JsonProperty("@odata.etag")
    protected String eTag;

    protected UUID id;

    protected Map<String, Map<String, Object>> references;
    protected Map<String, Object> formattedValues;

    public Entity() {
        this.attributes = new HashMap<>();
        this.formattedValues = new HashMap<>();
        this.references = new HashMap<>();
    }

    public String getETag() {
        return this.eTag;
    }

    public Object get(String attribute) {
        return attributes.get(attribute);
    }

    public boolean contains(String attribute) {
        return attributes.containsKey(attribute);
    }

    public Map<String, Object> getRef(String attribute) {
        return references.get(attribute);
    }

    public boolean containsRef(String attribute) {
        return references.containsKey(attribute);
    }

    @Override
    @JsonAnySetter
    protected void setAttributes(String name, Object value) {
        if (value != null) {
            if (name.startsWith("_")) {
                buildReferenceWithMetadata(name, value);
            }
            else if (name.contains("@") && name.contains("FormattedValue")) {
                formattedValues.put(name.split("@")[0], value);
            }
            else {
                attributes.put(name, value);
            }
        }
    }

    private void buildReferenceWithMetadata(String name, Object value) {
        String[] nameSplit = name.substring(1).split("_value|\\.");
        Map<String, Object> reference = references.containsKey(nameSplit[0]) ?
                references.get(nameSplit[0]) : new HashMap<String, Object>();

        if (nameSplit.length > 3) {
            reference.put(nameSplit[nameSplit.length - 1], value);
        }
        else {
            reference.put("id", UUID.fromString(value.toString()));
        }

        references.put(nameSplit[0], reference);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Map<String, Object> getFormattedValues() {
        return formattedValues;
    }

    public Map<String, Map<String, Object>> getReferences() {
        return references;
    }
}
