package com.notification.service;

import com.notification.dto.RoutingRuleRequest;
import com.notification.enums.MessageType;
import com.notification.model.RoutingRule;
import com.notification.repository.RoutingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingRuleService {

    private final RoutingRuleRepository routingRuleRepository;
    private final CacheManager cacheManager;

    @Cacheable(value = "routing-rules", key = "#messageType")
    public RoutingRule getRuleForMessageType(MessageType messageType) {
        Cache cache = cacheManager.getCache("routing-rules");
        if (cache != null && cache.get(messageType) != null) {
            log.info("CACHE HIT → Routing rule for {}", messageType);
        } else {
            log.info("CACHE MISS → Fetching routing rule from DB for {}", messageType);
        }
        return routingRuleRepository.findByMessageType(messageType);
    }

    public List<RoutingRule> getAllRules() {
        return StreamSupport.stream(routingRuleRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "routing-rules", allEntries = true)
    public RoutingRule createRule(RoutingRuleRequest request) {
        RoutingRule rule = RoutingRule.builder()
                .messageType(request.getMessageType())
                .channels(request.getChannels())
                .active(request.isActive())
                .retryCount(request.getRetryCount())
                .fallbackChannel(request.getFallbackChannel())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        routingRuleRepository.save(rule);
        log.info("Routing rule created for message type: {}", rule.getMessageType());
        return rule;
    }

    @CacheEvict(value = "routing-rules", allEntries = true)
    public RoutingRule updateRule(MessageType messageType, RoutingRuleRequest request) {
        RoutingRule existing = routingRuleRepository.findByMessageType(messageType);
        if (existing == null) {
            throw new IllegalArgumentException(
                    "Routing rule not found for message type: " + messageType);
        }
        existing.setChannels(request.getChannels());
        existing.setActive(request.isActive());
        existing.setRetryCount(request.getRetryCount());
        existing.setFallbackChannel(request.getFallbackChannel());
        existing.setUpdatedAt(Instant.now());
        routingRuleRepository.save(existing);
        log.info("Routing rule updated for message type: {}", messageType);
        return existing;
    }

    @CacheEvict(value = "routing-rules", allEntries = true)
    public void deleteRule(MessageType messageType) {

        RoutingRule existing = routingRuleRepository.findByMessageType(messageType);
        if (existing == null) {
            throw new IllegalArgumentException(
                    "Routing rule not found for message type: " + messageType);
        }
        routingRuleRepository.delete(existing);
        log.info("Routing rule deleted for message type: {}", messageType);
    }
}