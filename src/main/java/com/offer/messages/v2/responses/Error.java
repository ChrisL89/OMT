package com.offer.messages.v2.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.offer.messages.v2.MessageType;

/**
 * Created by erwan on 13/10/15.
 *
 * The Error Message Type.
 */
public class Error extends AbstractResponse {

    @JsonProperty("stream")
    private String stream;

    @JsonProperty("code")
    private int code;

    @JsonProperty("description")
    private String description;

    public Error(MessageType messageType) {
        super(MessageType.ERROR);
    }

    public String getStream() {
        return stream;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
