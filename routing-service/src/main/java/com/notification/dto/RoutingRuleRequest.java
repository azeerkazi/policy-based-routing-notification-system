package com.notification.dto;

import com.notification.enums.ChannelType;
import com.notification.enums.MessageType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RoutingRuleRequest {
    @NotNull
    private MessageType messageType;

    @NotNull
    private List<ChannelType> channels;
    private boolean active = true;
    private int retryCount = 0;
    private ChannelType fallbackChannel;
}