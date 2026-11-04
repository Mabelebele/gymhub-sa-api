package za.co.gymhubsa.api.loyalty.dto;

import za.co.gymhubsa.api.loyalty.entity.LoyaltyTransaction;
import za.co.gymhubsa.api.loyalty.entity.LoyaltyTransactionType;

import java.time.Instant;

public record LoyaltyTransactionResponse(
        Long id,
        Long memberId,
        LoyaltyTransactionType type,
        Integer points,
        String reason,
        Integer balanceAfter,
        Long rewardId,
        String rewardName,
        Instant createdAt
) {
    public static LoyaltyTransactionResponse from(LoyaltyTransaction transaction) {
        return new LoyaltyTransactionResponse(
                transaction.getId(),
                transaction.getMember().getId(),
                transaction.getType(),
                transaction.getPoints(),
                transaction.getReason(),
                transaction.getBalanceAfter(),
                transaction.getReward() != null ? transaction.getReward().getId() : null,
                transaction.getReward() != null ? transaction.getReward().getName() : null,
                transaction.getCreatedAt()
        );
    }
}
