package com.offer.messages.v2.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.offer.messages.v2.MessageType;

/**
 * Created by erwan on 7/10/15.
 *
 * StartSnapshot Response
 */
public class StartSnapshotResponse extends AbstractResponse {

    @JsonProperty("snapshot_queue")
    private String snapshotQueue;

    public StartSnapshotResponse(MessageType messageType) {
        super(messageType);
    }

    public String getSnapshotQueue() {
        return snapshotQueue;
    }
}
