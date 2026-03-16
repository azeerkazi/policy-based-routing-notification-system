package com.notification.repository;

import com.notification.enums.MessageType;
import com.notification.model.RoutingRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RoutingRuleRepository {

    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;

    private DynamoDbTable<RoutingRule> getTable() {
        return dynamoDbEnhancedClient.table("routing_rules", TableSchema.fromBean(RoutingRule.class));
    }

    public void save(RoutingRule rule) {
        try {
            getTable().putItem(rule);
            log.debug("Saved routing rule for message type: {}", rule.getMessageType());
        } catch (Exception e) {
            log.error("Failed to save routing rule for message type: {}", rule.getMessageType(), e);
            throw new RuntimeException("Failed to save routing rule", e);
        }
    }

    public RoutingRule findByMessageType(MessageType messageType) {
        try {
            return getTable().getItem(r -> r.key(k -> k.partitionValue(messageType.toString())));
        } catch (Exception e) {
            log.error("Failed to fetch routing rule for message type: {}", messageType, e);
            throw new RuntimeException("Failed to fetch routing rule", e);
        }
    }

    public Iterable<RoutingRule> findAll() {
        try {
            PageIterable<RoutingRule> results = getTable().scan();
            return results.items();
        } catch (Exception e) {
            log.error("Failed to fetch all routing rules", e);
            throw new RuntimeException("Failed to fetch all routing rules", e);
        }
    }

    public void delete(RoutingRule rule) {
        try {
            getTable().deleteItem(rule);
            log.debug("Deleted routing rule for message type: {}", rule.getMessageType());
        } catch (Exception e) {
            log.error("Failed to delete routing rule for message type: {}", rule.getMessageType(), e);
            throw new RuntimeException("Failed to delete routing rule", e);
        }
    }
}