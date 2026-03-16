package com.notification.repository;

import com.notification.model.UserPreference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
@RequiredArgsConstructor
public class UserPreferenceRepository {
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;

    private DynamoDbTable<UserPreference> getTable() {
        return dynamoDbEnhancedClient.table("user_preferences", TableSchema.fromBean(UserPreference.class));
    }

    public UserPreference findByUserId(String userId) {
        return getTable().getItem(r -> r.key(k -> k.partitionValue(userId)));
    }
}