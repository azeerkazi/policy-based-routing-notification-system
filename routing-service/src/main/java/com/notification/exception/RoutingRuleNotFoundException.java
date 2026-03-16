package com.notification.exception;

public class RoutingRuleNotFoundException extends RuntimeException {
    
    public RoutingRuleNotFoundException(String messageType) {
        super("No active routing rule found for message type: " + messageType);
    }
}