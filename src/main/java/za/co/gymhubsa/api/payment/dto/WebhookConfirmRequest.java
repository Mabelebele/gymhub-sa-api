package za.co.gymhubsa.api.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WebhookConfirmRequest(
        @NotBlank(message = "Gateway reference is required")
        String gatewayReference,

        @NotNull(message = "Success flag is required")
        Boolean success
) {
}
