
package com.reltio.cst.dataload.domain;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Crosswalk {

    @SerializedName("uri")
    @Expose
    private String uri;

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("value")
    @Expose
    private String value;

    @SerializedName("sourceTable")
    @Expose
    private String sourceTable;

    @SerializedName("dataProvider")
    @Expose
    private boolean dataProvider;

    @SerializedName("contributorProvider")
    @Expose
    private boolean contributorProvider;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(boolean dataProvider) {
        this.dataProvider = dataProvider;
    }

    public boolean isDataProvider() {
        return dataProvider;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Crosswalk{");
        sb.append("uri='").append(uri).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append(", sourceTable='").append(sourceTable).append('\'');
        sb.append(", dataProvider=").append(dataProvider);
        sb.append(", contributorProvider=").append(contributorProvider);
        sb.append('}');
        return sb.toString();
    }
}