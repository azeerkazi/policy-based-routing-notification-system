package com.notification.service;

import com.notification.dto.NotificationRequest;
import com.notification.enums.ChannelType;
import com.notification.enums.MessagePriority;
import com.notification.enums.MessageState;
import com.notification.exception.RoutingRuleNotFoundException;
import com.notification.publisher.SqsMessageSender;
import com.notification.model.Notification;
import com.notification.model.RoutingRule;
import com.notification.model.UserPreference;
import com.notification.channel.ChannelRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationPersistenceService persistenceService;
    private final RoutingRuleService routingRuleService;
    private final UserPreferenceService userPreferenceService;
    private final SqsMessageSender sqsMessageSender;
    private final ChannelRegistry channelRegistry;

    @Async("notificationTaskExecutor")
    public void processNotificationsAsync(List<NotificationRequest> requests) {
        processNotifications(requests);
    }

    private void processNotifications(List<NotificationRequest> requests) {

        List<NotificationRequest> sorted = requests.stream()
                .sorted(Comparator.comparing(NotificationRequest::getPriority,
                        Comparator.comparingInt(this::priorityOrder)))
                .collect(Collectors.toList());

        for (NotificationRequest request : sorted) {
            try {
                processSingleNotification(request);
            } catch (Exception e) {
                log.error("Failed to process notification for user: {}", request.getUserId(), e);
            }
        }
    }

    private int priorityOrder(MessagePriority priority) {
        if (priority == null)
            return Integer.MAX_VALUE;

        switch (priority) {
            case HIGH:
                return 1;
            case MEDIUM:
                return 2;
            case LOW:
                return 3;
            default:
                return 4;
        }
    }

    private void processSingleNotification(NotificationRequest request) {

        Notification notification = Notification.builder()
                .notificationId(UUID.randomUUID().toString())
                .userId(request.getUserId())
                .messageType(request.getMessageType())
                .messagePriority(request.getPriority())
                .payload(request.getPayload())
                .messageState(MessageState.RECEIVED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        persistenceService.saveNotification(notification);
        log.debug("Notification {} saved with state RECEIVED", notification.getNotificationId());

        updateState(notification, MessageState.ROUTING);

        try {

            List<ChannelType> channels = resolveChannels(notification);
            notification.setResolvedChannels(channels);

            if (channels.isEmpty()) {

                log.warn("No channels resolved for notification {}", notification.getNotificationId());

                notification.setErrorMessage("No channels resolved for user preferences");

                updateState(notification, MessageState.ROUTING_FAILED);
                return;
            }

            updateState(notification, MessageState.ROUTED);

            UserPreference userPref = userPreferenceService.getUserPreference(notification.getUserId());

            for (ChannelType channel : channels) {
                sqsMessageSender.sendToChannel(notification, userPref, channel);
            }

            log.info("Notification {} successfully routed to channels: {}",
                    notification.getNotificationId(), channels);

        } catch (Exception e) {

            log.error("Error processing notification {}", notification.getNotificationId(), e);

            notification.setErrorMessage(e.getMessage());

            updateState(notification, MessageState.ROUTING_FAILED);
        }
    }

    private void updateState(Notification notification, MessageState state) {
        notification.setMessageState(state);
        notification.setUpdatedAt(Instant.now());
        persistenceService.saveNotification(notification);
    }

    public List<ChannelType> resolveChannels(Notification notification) {

        RoutingRule rule = routingRuleService.getRuleForMessageType(notification.getMessageType());

        if (rule == null || !rule.isActive()) {
            throw new RoutingRuleNotFoundException(
                    "No active routing rule for message type: " + notification.getMessageType());
        }

        UserPreference userPref = userPreferenceService.getUserPreference(notification.getUserId());

        Map<String, Boolean> preferences = userPref != null ? userPref.getChannelPreferences() : Collections.emptyMap();

        List<ChannelType> resolved = new ArrayList<>();

        for (ChannelType channel : rule.getChannels()) {

            boolean userOptedIn = preferences.getOrDefault(channel.name(), false);
            boolean channelAvailable = channelRegistry.containsChannel(channel);

            if (userOptedIn && channelAvailable) {
                resolved.add(channel);
            }
        }

        if (resolved.isEmpty() && rule.getFallbackChannel() != null) {

            ChannelType fallback = rule.getFallbackChannel();

            boolean userOptedIn = preferences.getOrDefault(fallback.name(), false);
            boolean available = channelRegistry.containsChannel(fallback);

            if (userOptedIn && available) {
                resolved.add(fallback);
            }
        }

        return resolved;
    }
}