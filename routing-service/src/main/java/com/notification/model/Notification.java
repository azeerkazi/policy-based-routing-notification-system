package com.notification.model;

import java.time.Instant;
import java.util.List;

import com.notification.enums.ChannelType;
import com.notification.enums.MessagePriority;
import com.notification.enums.MessageState;
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
public class Notification {

    private String notificationId;
    private String userId;
    private MessageType messageType;
    private MessagePriority messagePriority;
    private String payload;
    private MessageState messageState;
    private List<ChannelType> resolvedChannels;
    private Instant createdAt;
    private Instant updatedAt;
    private String errorMessage;

    @DynamoDbPartitionKey
    public String getNotificationId() {
        return notificationId;
    }
}
