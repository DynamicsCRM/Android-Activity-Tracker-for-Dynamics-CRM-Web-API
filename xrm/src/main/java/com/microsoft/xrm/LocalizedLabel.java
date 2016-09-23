package com.microsoft.xrm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class LocalizedLabel {

    @JsonProperty("Label")
    private String label;

    @JsonProperty("LanguageCode")
    private String languageCode;

    @JsonProperty("IsManaged")
    private String isManaged;

    @JsonProperty("MetadataId")
    private UUID metadataId;

}
