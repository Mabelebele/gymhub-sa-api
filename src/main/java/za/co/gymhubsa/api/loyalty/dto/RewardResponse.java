package za.co.gymhubsa.api.loyalty.dto;

import za.co.gymhubsa.api.loyalty.entity.Reward;

public record RewardResponse(
        Long id,
        String name,
        String description,
        Integer pointsCost,
        Integer stockQuantity,
        boolean active
) {
    public static RewardResponse from(Reward reward) {
        return new RewardResponse(reward.getId(), reward.getName(), reward.getDescription(),
                reward.getPointsCost(), reward.getStockQuantity(), reward.isActive());
    }
}
