package com.offer.messages.v2.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.offer.messages.v2.MessageType;

/**
 * Created by erwan on 7/10/15.
 *
 * StopSessionRequest.
 */
public class StopSessionRequest extends AbstractRequest {

    @Override
    @JsonIgnore
    public MessageType getMessageType() {
        return MessageType.STOP_SESSION_REQUEST;
    }

}
