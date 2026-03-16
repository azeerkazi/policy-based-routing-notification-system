package com.notification.controller;

import com.notification.dto.NotificationRequest;
import com.notification.model.Notification;
import com.notification.service.NotificationPersistenceService;
import com.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationPersistenceService persistenceService;

    @PostMapping
    public ResponseEntity<String> sendNotifications(@Valid @RequestBody List<NotificationRequest> requests) {
        log.info("Received {} notification request(s)", requests.size());
        notificationService.processNotificationsAsync(requests);
        return ResponseEntity.accepted().body("Notification processing started");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationStatus(@PathVariable String id) {
        log.info("Fetching status for notification: {}", id);
        Notification notification = persistenceService.getNotification(id);
        if (notification == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(notification);
    }
}