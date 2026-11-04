package za.co.gymhubsa.api.loyalty.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.loyalty.dto.RewardRequest;
import za.co.gymhubsa.api.loyalty.dto.RewardResponse;
import za.co.gymhubsa.api.loyalty.entity.Reward;
import za.co.gymhubsa.api.loyalty.repository.RewardRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final RewardRepository rewardRepository;

    @Transactional
    public RewardResponse create(RewardRequest request) {
        Reward reward = Reward.builder()
                .name(request.name())
                .description(request.description())
                .pointsCost(request.pointsCost())
                .stockQuantity(request.stockQuantity())
                .active(true)
                .build();
        rewardRepository.save(reward);
        return RewardResponse.from(reward);
    }

    @Transactional(readOnly = true)
    public List<RewardResponse> list(boolean includeInactive) {
        List<Reward> rewards = includeInactive ? rewardRepository.findAll() : rewardRepository.findByActiveTrue();
        return rewards.stream().map(RewardResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public RewardResponse getById(Long id) {
        return RewardResponse.from(findRewardOrThrow(id));
    }

    @Transactional
    public RewardResponse update(Long id, RewardRequest request) {
        Reward reward = findRewardOrThrow(id);
        reward.setName(request.name());
        reward.setDescription(request.description());
        reward.setPointsCost(request.pointsCost());
        reward.setStockQuantity(request.stockQuantity());
        rewardRepository.save(reward);
        return RewardResponse.from(reward);
    }

    /** Soft-deletes a reward (marks inactive) so past redemption transactions keep a valid reference. */
    @Transactional
    public void deactivate(Long id) {
        Reward reward = findRewardOrThrow(id);
        reward.setActive(false);
        rewardRepository.save(reward);
    }

    private Reward findRewardOrThrow(Long id) {
        return rewardRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Reward", id));
    }
}
