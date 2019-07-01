
package com.reltio.cst.dataload.domain;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EntityResponse {

    @SerializedName("index")
    @Expose
    private Integer index;

    @SerializedName("uri")
    @Expose
    private String uri;

    @SerializedName("successful")
    @Expose
    private Boolean successful;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Boolean getSuccessful() {
        return successful;
    }

    public void setSuccessful(Boolean successful) {
        this.successful = successful;
    }

}
