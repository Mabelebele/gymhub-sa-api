package za.co.gymhubsa.api.loyalty.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.loyalty.dto.RewardRequest;
import za.co.gymhubsa.api.loyalty.dto.RewardResponse;
import za.co.gymhubsa.api.loyalty.entity.Reward;
import za.co.gymhubsa.api.loyalty.repository.RewardRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @Mock
    private RewardRepository rewardRepository;

    private RewardService rewardService;

    @BeforeEach
    void setUp() {
        rewardService = new RewardService(rewardRepository);
    }

    @Test
    void create_savesActiveReward() {
        RewardResponse response = rewardService.create(new RewardRequest("Free Shake", "Protein shake", 50, 20));

        assertThat(response.name()).isEqualTo("Free Shake");
        assertThat(response.active()).isTrue();
    }

    @Test
    void getById_whenMissing_throwsNotFound() {
        when(rewardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rewardService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deactivate_setsActiveFalse() {
        Reward reward = Reward.builder().name("Old Reward").pointsCost(10).active(true).build();
        reward.setId(1L);
        when(rewardRepository.findById(1L)).thenReturn(Optional.of(reward));

        rewardService.deactivate(1L);

        assertThat(reward.isActive()).isFalse();
    }
}
