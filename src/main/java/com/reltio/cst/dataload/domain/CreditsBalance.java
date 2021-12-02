package com.reltio.cst.dataload.domain;

import com.google.gson.annotations.SerializedName;

public class CreditsBalance {
    @SerializedName("primary")
    CreditsGroupBalance primaryBalance;

    public CreditsGroupBalance getPrimaryBalance() {
        return primaryBalance;
    }
}
