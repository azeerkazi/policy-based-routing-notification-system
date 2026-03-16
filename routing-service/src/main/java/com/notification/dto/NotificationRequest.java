package com.notification.dto;

import com.notification.enums.MessagePriority;
import com.notification.enums.MessageType;

import lombok.Data;

@Data
public class NotificationRequest {

    private String userId;
    private MessageType messageType;
    private MessagePriority priority;
    private String payload;
}
