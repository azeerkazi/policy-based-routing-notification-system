package com.notification.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EmailNotification(
                String notificationId,
                String recipient,
                String payload) {
}