package com.notification.repository;

import com.notification.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
@RequiredArgsConstructor
public class NotificationRepository {
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;

    private DynamoDbTable<Notification> getTable() {
        return dynamoDbEnhancedClient.table("messages", TableSchema.fromBean(Notification.class));
    }

    public void save(Notification notification) {
        getTable().putItem(notification);
    }

    public Notification findById(String notificationId) {
        return getTable().getItem(r -> r.key(k -> k.partitionValue(notificationId)));
    }
}