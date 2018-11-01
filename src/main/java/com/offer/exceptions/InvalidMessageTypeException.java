package com.offer.exceptions;

/**
 * Created by erwan on 6/10/15.
 *
 * This is thrown when a Response object could not be created from the message_type header.
 */
public class InvalidMessageTypeException extends Exception {
    public InvalidMessageTypeException(String message) {
        super(message);
    }

    public InvalidMessageTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
