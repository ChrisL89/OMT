package com.offer.messages.v2.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.offer.messages.v2.Message;
import com.offer.messages.v2.MessageType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by erwan on 1/10/15.
 *
 * Abstract Response for Tamale v2
 */
abstract public class AbstractResponse implements Message {

    private MessageType messageType;
    protected String correlationId;
    protected Map<String, Object> messageHeaders;

    public AbstractResponse(MessageType messageType) {
        this.messageType = messageType;
    }

    @Override
    @JsonIgnore
    public MessageType getMessageType() {
        return messageType;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public void setMessageHeaders(Map<String, Object> messageHeaders) {
        this.messageHeaders = messageHeaders;
    }

    @JsonIgnore
    public HashMap<String, Object> getMessageHeaders() {
        return new HashMap<>(messageHeaders);
    }

    @JsonIgnore
    public String getCorrelationId() {
        return correlationId;
    }

}
