package za.co.gymhubsa.api.attendance.dto;

import jakarta.validation.constraints.NotBlank;

public record QrCheckInRequest(
        @NotBlank(message = "QR code is required")
        String qrCode
) {
}
