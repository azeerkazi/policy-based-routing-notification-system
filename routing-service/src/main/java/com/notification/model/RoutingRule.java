package com.notification.model;

import java.time.Instant;
import java.util.List;
import com.notification.enums.ChannelType;
import com.notification.enums.MessageType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class RoutingRule {

    private MessageType messageType;
    private List<ChannelType> channels;
    private boolean active;
    private int retryCount;
    private ChannelType fallbackChannel;
    private Instant createdAt;
    private Instant updatedAt;

    @DynamoDbPartitionKey
    public MessageType getMessageType() {
        return messageType;
    }

}
