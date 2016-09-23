package com.microsoft.xrm;


import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class BaseResponse {

    @JsonProperty("@odata.context")
    protected String context;

    protected Map<String, Object> attributes;

    BaseResponse() {
        attributes = new HashMap<>();
    }

    @JsonAnySetter
    protected void setAttributes(String name, Object value) {
        attributes.put(name, value);
    }

    public String getContext() {
        return context;
    }
}
