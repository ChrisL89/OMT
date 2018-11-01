package com.offer.messages.v2.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.offer.messages.v2.MessageType;

/**
 * Created by erwan on 7/10/15.
 *
 * END_SNAPSHOT_MARKER
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EndSnapshotMarker extends AbstractResponse {

    @JsonProperty("delta_queue")
    private String deltaQueue;

    public EndSnapshotMarker(MessageType messageType) {
        super(messageType);
    }

    public String getDeltaQueue() {
        return deltaQueue;
    }

}
