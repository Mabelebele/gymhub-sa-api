package za.co.gymhubsa.api.attendance.dto;

import jakarta.validation.constraints.NotNull;

public record CheckInRequest(
        @NotNull(message = "Member id is required")
        Long memberId
) {
}
