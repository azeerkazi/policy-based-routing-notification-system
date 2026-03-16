package com.notification.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChannelMessage {

    private String notificationId;
    private String recipient;
    private String payload;
}