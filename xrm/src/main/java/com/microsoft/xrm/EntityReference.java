package com.microsoft.xrm;

import java.util.UUID;

public final class EntityReference {

    public final String LogicalName;
    public final UUID Id;

    public EntityReference(String logicalName, UUID id) {
        this.LogicalName = logicalName;
        this.Id = id;
    }

}
