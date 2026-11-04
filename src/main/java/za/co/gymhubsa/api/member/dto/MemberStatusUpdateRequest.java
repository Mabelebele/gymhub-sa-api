package za.co.gymhubsa.api.member.dto;

import jakarta.validation.constraints.NotNull;
import za.co.gymhubsa.api.member.entity.MemberStatus;

public record MemberStatusUpdateRequest(
        @NotNull(message = "Status is required")
        MemberStatus status
) {
}
