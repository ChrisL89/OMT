package com.offer.messages.v2.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.offer.messages.v2.MessageType;

/**
 * Created by erwan on 5/10/15.
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StartSessionResponse extends AbstractResponse {

    @JsonProperty("delta_queue")
    private String deltaQueue;

    public StartSessionResponse(MessageType messageType) {
        super(messageType);
    }

    public String getDeltaQueue() {
        return deltaQueue;
    }
}
