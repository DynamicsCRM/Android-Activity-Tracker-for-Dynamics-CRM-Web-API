package com.microsoft.xrm;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public final class EntityCollection {

    @JsonProperty("@odata.context")
    private String context;

    @JsonProperty("@odata.nextLink")
    private String nextPage;

    @JsonProperty("value")
    private List<Entity> Entities;

    public EntityCollection() {
        Entities = new ArrayList<>();
    }

    public List<Entity> getEntities() {
        return Entities;
    }

    public String getNextPage() {
        return nextPage;
    }

    public boolean hasNextPage() {
        return nextPage != null && !nextPage.equals("");
    }

    public String getContext() {
        return context;
    }
}
