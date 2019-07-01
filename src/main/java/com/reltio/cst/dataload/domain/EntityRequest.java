
package com.reltio.cst.dataload.domain;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EntityRequest {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("crosswalks")
    @Expose
    private List<Crosswalk> crosswalks = null;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Crosswalk> getCrosswalks() {
        return crosswalks;
    }

    public void setCrosswalks(List<Crosswalk> crosswalks) {
        this.crosswalks = crosswalks;
    }

}
