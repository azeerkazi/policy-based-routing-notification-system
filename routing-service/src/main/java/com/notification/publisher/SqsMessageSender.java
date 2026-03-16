package com.notification.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.dto.ChannelMessage;
import com.notification.enums.ChannelType;
import com.notification.model.Notification;
import com.notification.model.UserPreference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class SqsMessageSender {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${sqs.email-queue-url}")
    private String emailQueueUrl;

    @Value("${sqs.sms-queue-url}")
    private String smsQueueUrl;

    public void sendToChannel(Notification notification, UserPreference userPref, ChannelType channelType) {

        String queueUrl = resolveQueueUrl(channelType);

        try {
            String recipient;
            switch (channelType) {
                case EMAIL:
                    recipient = userPref.getEmail();
                    break;
                case SMS:
                    recipient = userPref.getPhoneNumber();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported channel type: " + channelType);
            }
            ChannelMessage message = ChannelMessage.builder()
                    .notificationId(notification.getNotificationId())
                    .recipient(recipient)
                    .payload(notification.getPayload())
                    .build();

            String messageBody = objectMapper.writeValueAsString(message);
            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .build();
            SendMessageResponse response = sqsClient.sendMessage(request);
            log.info("Notification {} sent to {} queue. MessageId: {}",
                    notification.getNotificationId(), channelType, response.messageId());

        } catch (Exception e) {
            throw new RuntimeException("SQS send failed", e);
        }
    }

    private String resolveQueueUrl(ChannelType channelType) {
        switch (channelType) {
            case EMAIL:
                return emailQueueUrl;
            case SMS:
                return smsQueueUrl;
            default:
                throw new IllegalArgumentException("Unsupported channel type: " + channelType);
        }
    }
}