package com.offer.messages.v2.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.offer.messages.v2.MessageType;

/**
 * Created by erwan on 1/10/15.
 *
 * The StartSession Request object.
 */
public class StartSessionRequest extends AbstractRequest {

    @Override
    @JsonIgnore
    public MessageType getMessageType() {
        return MessageType.START_SESSION_REQUEST;
    }

}
