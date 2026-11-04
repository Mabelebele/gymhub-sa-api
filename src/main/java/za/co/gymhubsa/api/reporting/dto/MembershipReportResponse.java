package za.co.gymhubsa.api.reporting.dto;

import java.util.List;

public record MembershipReportResponse(
        long activeCount,
        long expiredCount,
        long cancelledCount,
        List<PlanDistributionEntry> planDistribution
) {
}
