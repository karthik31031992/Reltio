package com.reltio.cst.dataload.domain;

import com.google.gson.annotations.SerializedName;

public class CreditsGroupBalance {
    @SerializedName("priorityCredits")
    private Float priorityCredits;
    @SerializedName("standardSyncCredits")
    private Float standardSyncCredits;
    @SerializedName("standardAsyncCredits")
    private Float standardAsyncCredits;

    public Float getPriorityCredits() {
        return priorityCredits;
    }

    public Float getStandardSyncCredits() {
        return standardSyncCredits;
    }

    public Float getStandardAsyncCredits() {
        return standardAsyncCredits;
    }
}
