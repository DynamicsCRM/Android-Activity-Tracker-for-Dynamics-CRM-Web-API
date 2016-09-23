package com.microsoft.xrm;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public final class DefinitionMetadataResponse {

    @JsonProperty("@odata.context")
    private String context;

    @JsonProperty("@odata.nextLink")
    private String nextPage;

    @JsonProperty("value")
    private List<EntityDefinition> definitions;

    public DefinitionMetadataResponse() {
        definitions = new ArrayList<>();
    }

    public String getContext() {
        return this.context;
    }

    public List<EntityDefinition> getDefinitions() {
        return this.definitions;
    }

    public String getNextPage() {
        return this.nextPage;
    }
}
