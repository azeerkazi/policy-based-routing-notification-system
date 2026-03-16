package com.notification.service;

import com.notification.model.Notification;
import com.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPersistenceService {
    private final NotificationRepository notificationRepository;

    public void saveNotification(Notification notification) {
        notification.setUpdatedAt(Instant.now());
        notificationRepository.save(notification);
    }

    public Notification getNotification(String notificationId) {
        return notificationRepository.findById(notificationId);
    }
}