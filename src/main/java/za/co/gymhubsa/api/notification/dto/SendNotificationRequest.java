package za.co.gymhubsa.api.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import za.co.gymhubsa.api.notification.entity.NotificationChannel;
import za.co.gymhubsa.api.notification.entity.NotificationType;

public record SendNotificationRequest(
        @NotNull(message = "Member id is required")
        Long memberId,

        @NotNull(message = "Channel is required")
        NotificationChannel channel,

        @NotNull(message = "Type is required")
        NotificationType type,

        String subject,

        @NotBlank(message = "Message is required")
        String message
) {
}
