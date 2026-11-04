package za.co.gymhubsa.api.attendance.dto;

import jakarta.validation.constraints.NotNull;

public record CheckOutRequest(
        @NotNull(message = "Member id is required")
        Long memberId
) {
}
