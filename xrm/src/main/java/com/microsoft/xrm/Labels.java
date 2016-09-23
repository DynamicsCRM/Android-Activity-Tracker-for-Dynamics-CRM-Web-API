package com.microsoft.xrm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Labels {

    @JsonProperty("LocalizedLabels")
    private List<LocalizedLabel> localizedLabels;

    @JsonProperty("UserLocalizedLabel")
    private LocalizedLabel userLocalizedLabel;

    public Labels() {
        this.localizedLabels = new ArrayList<>();
    }

    public List<LocalizedLabel> getLocalizedLabels() {
        return localizedLabels;
    }

    public LocalizedLabel getUserLocalizedLabel() {
        return userLocalizedLabel;
    }
}
