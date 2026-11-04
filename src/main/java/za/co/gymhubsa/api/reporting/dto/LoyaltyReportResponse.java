package za.co.gymhubsa.api.reporting.dto;

import java.util.List;

public record LoyaltyReportResponse(
        long totalPointsEarned,
        long totalPointsRedeemed,
        List<LoyaltyTopEarnerEntry> topEarners
) {
}
