package com.reltio.cst.dataload.domain;

import com.google.gson.annotations.SerializedName;

public class StatusResponse {

    @SerializedName("Status")
    private String status;
    @SerializedName("IsExternalQueue")
    private String isExternalQueue;
    @SerializedName("Events queue size")
    private Long eventsQueueSize;
    @SerializedName("Matching queue size")
    private Long matchingQueueSize;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIsExternalQueue() {
        return isExternalQueue;
    }

    public void setIsExternalQueue(String isExternalQueue) {
        this.isExternalQueue = isExternalQueue;
    }

    public Long getEventsQueueSize() {
        return eventsQueueSize;
    }

    public void setEventsQueueSize(Long eventsQueueSize) {
        this.eventsQueueSize = eventsQueueSize;
    }

    public Long getMatchingQueueSize() {
        return matchingQueueSize;
    }

    public void setMatchingQueueSize(Long matchingQueueSize) {
        this.matchingQueueSize = matchingQueueSize;
    }
}
