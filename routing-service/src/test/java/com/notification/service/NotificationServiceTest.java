package com.notification.service;

import com.notification.channel.ChannelRegistry;
import com.notification.enums.ChannelType;
import com.notification.enums.MessageType;
import com.notification.model.Notification;
import com.notification.model.RoutingRule;
import com.notification.model.UserPreference;
import com.notification.publisher.SqsMessageSender;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationPersistenceService persistenceService;

    @Mock
    private RoutingRuleService routingRuleService;

    @Mock
    private UserPreferenceService userPreferenceService;

    @Mock
    private SqsMessageSender sqsMessageSender;

    @Mock
    private ChannelRegistry channelRegistry;

    @InjectMocks
    private NotificationService notificationService;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .notificationId("test-notification")
                .userId("user-123")
                .messageType(MessageType.CRITICAL)
                .build();
    }

    private UserPreference buildPreference(Map<String, Boolean> prefs) {
        return UserPreference.builder()
                .userId("user-123")
                .channelPreferences(prefs)
                .build();
    }

    private RoutingRule buildRule(List<ChannelType> channels) {
        return RoutingRule.builder()
                .messageType(MessageType.CRITICAL)
                .channels(channels)
                .active(true)
                .build();
    }

    @Test
    void shouldResolveBothChannelsWhenRuleAndPreferenceAllowBoth() {

        RoutingRule rule = buildRule(List.of(ChannelType.EMAIL, ChannelType.SMS));

        Map<String, Boolean> prefs = Map.of(
                "EMAIL", true,
                "SMS", true);

        when(routingRuleService.getRuleForMessageType(MessageType.CRITICAL)).thenReturn(rule);
        when(userPreferenceService.getUserPreference("user-123")).thenReturn(buildPreference(prefs));
        when(channelRegistry.containsChannel(ChannelType.EMAIL)).thenReturn(true);
        when(channelRegistry.containsChannel(ChannelType.SMS)).thenReturn(true);

        List<ChannelType> result = notificationService.resolveChannels(notification);

        assertEquals(2, result.size());
        assertTrue(result.contains(ChannelType.EMAIL));
        assertTrue(result.contains(ChannelType.SMS));
    }

    @Test
    void shouldResolveOnlyEmailWhenSmsDisabledByUser() {

        RoutingRule rule = buildRule(List.of(ChannelType.EMAIL, ChannelType.SMS));

        Map<String, Boolean> prefs = Map.of(
                "EMAIL", true,
                "SMS", false);

        when(routingRuleService.getRuleForMessageType(MessageType.CRITICAL)).thenReturn(rule);
        when(userPreferenceService.getUserPreference("user-123")).thenReturn(buildPreference(prefs));
        when(channelRegistry.containsChannel(ChannelType.EMAIL)).thenReturn(true);
        when(channelRegistry.containsChannel(ChannelType.SMS)).thenReturn(true);

        List<ChannelType> result = notificationService.resolveChannels(notification);

        assertEquals(1, result.size());
        assertEquals(ChannelType.EMAIL, result.get(0));
    }

    @Test
    void shouldReturnEmptyWhenRuleAllowsSmsButUserDisabledIt() {

        RoutingRule rule = buildRule(List.of(ChannelType.SMS));

        Map<String, Boolean> prefs = Map.of(
                "SMS", false);

        when(routingRuleService.getRuleForMessageType(MessageType.CRITICAL)).thenReturn(rule);
        when(userPreferenceService.getUserPreference("user-123")).thenReturn(buildPreference(prefs));
        when(channelRegistry.containsChannel(ChannelType.SMS)).thenReturn(true);

        List<ChannelType> result = notificationService.resolveChannels(notification);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldIgnoreChannelWhenRegistryDoesNotContainIt() {

        RoutingRule rule = buildRule(List.of(ChannelType.EMAIL));

        Map<String, Boolean> prefs = Map.of(
                "EMAIL", true);

        when(routingRuleService.getRuleForMessageType(MessageType.CRITICAL)).thenReturn(rule);
        when(userPreferenceService.getUserPreference("user-123")).thenReturn(buildPreference(prefs));
        when(channelRegistry.containsChannel(ChannelType.EMAIL)).thenReturn(false);

        List<ChannelType> result = notificationService.resolveChannels(notification);

        assertTrue(result.isEmpty());
    }

    @Test

    void shouldUseFallbackChannelWhenPrimaryChannelsNotResolved() {
        RoutingRule rule = RoutingRule.builder()
                .messageType(MessageType.CRITICAL)
                .channels(List.of(ChannelType.SMS))
                .fallbackChannel(ChannelType.EMAIL)
                .active(true)
                .build();
        Map<String, Boolean> prefs = Map.of(
                "SMS", false,
                "EMAIL", true);

        when(routingRuleService.getRuleForMessageType(MessageType.CRITICAL)).thenReturn(rule);
        when(userPreferenceService.getUserPreference("user-123")).thenReturn(buildPreference(prefs));

        when(channelRegistry.containsChannel(ChannelType.SMS)).thenReturn(true);
        when(channelRegistry.containsChannel(ChannelType.EMAIL)).thenReturn(true);

        List<ChannelType> result = notificationService.resolveChannels(notification);

        assertEquals(1, result.size());
        assertEquals(ChannelType.EMAIL, result.get(0));
    }

    @Test
    void shouldThrowExceptionWhenNoRoutingRuleExists() {

        when(routingRuleService.getRuleForMessageType(MessageType.CRITICAL)).thenReturn(null);

        assertThrows(RuntimeException.class,
                () -> notificationService.resolveChannels(notification));
    }
}