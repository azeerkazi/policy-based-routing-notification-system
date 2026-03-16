package com.notification.dto;

import com.notification.enums.ChannelType;
import com.notification.enums.MessageType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class RoutingRuleResponse {
    private MessageType messageType;
    private List<ChannelType> channels;
    private boolean active;
    private int retryCount;
    private ChannelType fallbackChannel;
    private Instant createdAt;
    private Instant updatedAt;
}