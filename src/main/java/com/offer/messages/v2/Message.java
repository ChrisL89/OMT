package com.offer.messages.v2;

import java.util.Map;

/**
 * Created by erwan on 6/10/15.
 *
 */
public interface Message {
    public static final String VERSION_HEADER = "version";
    public static final String MESSAGE_TYPE_HEADER = "message_type";

    public MessageType getMessageType();
    public Map<String, Object> getMessageHeaders();
    public String getCorrelationId();

}
