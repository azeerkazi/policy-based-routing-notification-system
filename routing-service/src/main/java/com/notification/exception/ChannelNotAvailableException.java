package com.notification.exception;

public class ChannelNotAvailableException extends RuntimeException {
    public ChannelNotAvailableException(String message) {
        super(message);
    }
}