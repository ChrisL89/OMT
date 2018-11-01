package com.offer.messages.v2;

/**
 * Created by erwan on 1/10/15.
 *
 * MessageHeaders used by all requests and responses.
 *
 */
public class MessageHeaders {
    public static final int VERSION = 2;
    private String messageType;

    public MessageHeaders(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
