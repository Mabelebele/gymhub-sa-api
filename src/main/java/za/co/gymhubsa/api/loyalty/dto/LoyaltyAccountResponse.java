package za.co.gymhubsa.api.loyalty.dto;

import za.co.gymhubsa.api.loyalty.entity.LoyaltyAccount;

public record LoyaltyAccountResponse(
        Long memberId,
        Integer pointsBalance
) {
    public static LoyaltyAccountResponse from(LoyaltyAccount account) {
        return new LoyaltyAccountResponse(account.getMember().getId(), account.getPointsBalance());
    }
}
