package za.co.gymhubsa.api.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import za.co.gymhubsa.api.notification.entity.NotificationChannel;

import java.util.List;

public record PromotionalBroadcastRequest(
        @NotNull(message = "Channel is required")
        NotificationChannel channel,

        String subject,

        @NotBlank(message = "Message is required")
        String message,

        /** Specific member ids to target; if null/empty, broadcasts to all active members. */
        List<Long> memberIds
) {
}
