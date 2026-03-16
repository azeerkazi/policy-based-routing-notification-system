package com.notification.model;

import java.time.Instant;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class UserPreference {

    private String userId;
    private String email;
    private String phoneNumber;
    private Map<String, Boolean> channelPreferences;
    private Instant createdAt;
    private Instant updatedAt;

    @DynamoDbPartitionKey
    public String getUserId() {
        return userId;
    }
}