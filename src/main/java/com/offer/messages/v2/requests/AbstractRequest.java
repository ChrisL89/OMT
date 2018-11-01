package com.offer.messages.v2.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.offer.messages.v2.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by erwan on 1/10/15.
 *
 * Abstract Request for Tamale v2.
 */
abstract public class AbstractRequest implements Message {

    protected String correlationId;
    protected Map<String, Object> messageHeaders;
    private String stream;
    private String token;


    public AbstractRequest() {
        messageHeaders = new HashMap<>();
        messageHeaders.put(VERSION_HEADER, "2");
        messageHeaders.put(MESSAGE_TYPE_HEADER, getMessageType().toString().toLowerCase());
    }

    @JsonIgnore
    public String getCorrelationId() {
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        return correlationId;
    }

    @Override
    @JsonIgnore
    public Map<String, Object> getMessageHeaders() {
        return new HashMap<>(messageHeaders);
    }

    @JsonProperty("stream")
    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    @JsonProperty("token")
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
