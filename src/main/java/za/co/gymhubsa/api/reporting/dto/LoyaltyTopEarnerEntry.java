package za.co.gymhubsa.api.reporting.dto;

public record LoyaltyTopEarnerEntry(
        Long memberId,
        String memberFullName,
        Integer pointsBalance
) {
}
