package za.co.gymhubsa.api.reporting.dto;

import org.springframework.data.domain.Page;
import za.co.gymhubsa.api.member.dto.MemberResponse;

public record MemberReportResponse(
        long totalMembers,
        long activeCount,
        long inactiveCount,
        long suspendedCount,
        Page<MemberResponse> members
) {
}
