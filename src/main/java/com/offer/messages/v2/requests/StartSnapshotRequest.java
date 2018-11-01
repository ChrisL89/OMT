package com.offer.messages.v2.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.offer.messages.v2.MessageType;

/**
 * Created by erwan on 7/10/15.
 *
 * Start Snapshot Request
 */
public class StartSnapshotRequest extends AbstractRequest {

    @Override
    @JsonIgnore
    public MessageType getMessageType() {
        return MessageType.START_SNAPSHOT_REQUEST;
    }
}
