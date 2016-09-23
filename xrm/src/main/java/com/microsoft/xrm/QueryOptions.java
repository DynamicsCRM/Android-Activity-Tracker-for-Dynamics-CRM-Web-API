package com.microsoft.xrm;

import java.util.HashMap;

public final class QueryOptions extends HashMap<String, String> {

    public QueryOptions() {
        super();
    }

    public QueryOptions putExpand(String expand) {
        this.put("$expand", expand);
        return this;
    }

    public QueryOptions putFilter(String filter) {
        this.put("$filter", filter);
        return this;
    }

    public QueryOptions putSelect(String select) {
        this.put("$select", select);
        return this;
    }

    public QueryOptions putSelect(String... select) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String column : select) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(',');
            }
            stringBuilder.append(column);
        }
        this.put("$select", stringBuilder.toString());
        return this;
    }

    public QueryOptions putLinkedEntity(String attributeName) {
        String selectStatement = "";
        if (this.containsKey("$select")) {
            selectStatement = this.get("$select");
            selectStatement += ",";
        }

        selectStatement += "_" + attributeName + "_value";
        this.put("$select", selectStatement);
        return this;
    }

    public QueryOptions putOrderBy(String attributeName, boolean isAscending) {
        this.put("$orderby", attributeName + (!isAscending ? " desc" : " asc"));
        return this;
    }

}
