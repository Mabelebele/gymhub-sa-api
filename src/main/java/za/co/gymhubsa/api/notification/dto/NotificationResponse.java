package za.co.gymhubsa.api.notification.dto;

import za.co.gymhubsa.api.notification.entity.Notification;
import za.co.gymhubsa.api.notification.entity.NotificationChannel;
import za.co.gymhubsa.api.notification.entity.NotificationStatus;
import za.co.gymhubsa.api.notification.entity.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        Long memberId,
        String recipient,
        NotificationChannel channel,
        NotificationType type,
        String subject,
        String message,
        NotificationStatus status,
        Instant sentAt,
        String errorMessage
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getMember() != null ? notification.getMember().getId() : null,
                notification.getRecipient(),
                notification.getChannel(),
                notification.getType(),
                notification.getSubject(),
                notification.getMessage(),
                notification.getStatus(),
                notification.getSentAt(),
                notification.getErrorMessage()
        );
    }
}
