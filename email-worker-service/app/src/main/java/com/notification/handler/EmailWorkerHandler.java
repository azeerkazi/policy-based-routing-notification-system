package com.notification.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;

import com.notification.model.EmailNotification;
import com.notification.service.GmailEmailService;
import com.notification.util.JsonUtil;

import java.util.*;

public class EmailWorkerHandler implements RequestHandler<SQSEvent, Map<String, Object>> {

    private final GmailEmailService emailService;

    public EmailWorkerHandler() {
        this.emailService = new GmailEmailService();
    }

    @Override
    public Map<String, Object> handleRequest(SQSEvent event, Context context) {

        List<Map<String, String>> batchItemFailures = new ArrayList<>();

        for (SQSEvent.SQSMessage record : event.getRecords()) {

            String messageId = record.getMessageId();

            try {

                EmailNotification notification = JsonUtil.mapper.readValue(
                        record.getBody(),
                        EmailNotification.class);

                validate(notification);

                emailService.send(
                        notification.recipient(),
                        "Notification",
                        notification.payload());

                context.getLogger().log(
                        "EMAIL SENT | sqsMessageId=" + messageId +
                                " | notificationId=" + notification.notificationId());

            } catch (Exception ex) {

                context.getLogger().log(
                        "EMAIL FAILED | sqsMessageId=" + messageId +
                                " | error=" + ex.getMessage());

                batchItemFailures.add(
                        Map.of("itemIdentifier", messageId));
            }
        }

        return Map.of("batchItemFailures", batchItemFailures);
    }

    private void validate(EmailNotification n) {

        if (n.recipient() == null || !n.recipient().contains("@")) {
            throw new IllegalArgumentException("Invalid email recipient");
        }

        if (n.payload() == null || n.payload().isBlank()) {
            throw new IllegalArgumentException("Email payload missing");
        }
    }
}