package za.co.gymhubsa.api.loyalty.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;
import za.co.gymhubsa.api.common.exception.BadRequestException;
import za.co.gymhubsa.api.loyalty.dto.EarnPointsRequest;
import za.co.gymhubsa.api.loyalty.dto.LoyaltyAccountResponse;
import za.co.gymhubsa.api.loyalty.dto.LoyaltyTransactionResponse;
import za.co.gymhubsa.api.loyalty.entity.LoyaltyAccount;
import za.co.gymhubsa.api.loyalty.entity.Reward;
import za.co.gymhubsa.api.loyalty.repository.LoyaltyAccountRepository;
import za.co.gymhubsa.api.loyalty.repository.LoyaltyTransactionRepository;
import za.co.gymhubsa.api.loyalty.repository.RewardRepository;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.entity.MemberStatus;
import za.co.gymhubsa.api.member.repository.MemberRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoyaltyServiceTest {

    @Mock
    private LoyaltyAccountRepository loyaltyAccountRepository;
    @Mock
    private LoyaltyTransactionRepository loyaltyTransactionRepository;
    @Mock
    private RewardRepository rewardRepository;
    @Mock
    private MemberRepository memberRepository;

    private LoyaltyService loyaltyService;

    private Member member;

    @BeforeEach
    void setUp() {
        loyaltyService = new LoyaltyService(loyaltyAccountRepository, loyaltyTransactionRepository,
                rewardRepository, memberRepository);

        User user = User.builder().firstName("Palesa").lastName("Mofokeng").email("palesa@example.com")
                .password("hashed").role(Role.MEMBER).enabled(true).build();
        user.setId(1L);
        member = Member.builder().user(user).status(MemberStatus.ACTIVE).joinDate(LocalDate.now()).build();
        member.setId(1L);
    }

    @Test
    void earn_withNoExistingAccount_createsAccountAndAddsPoints() {
        when(loyaltyAccountRepository.findByMemberId(1L)).thenReturn(Optional.empty());
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(loyaltyAccountRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LoyaltyTransactionResponse response = loyaltyService.earn(new EarnPointsRequest(1L, 50, "Attendance streak"));

        assertThat(response.points()).isEqualTo(50);
        assertThat(response.balanceAfter()).isEqualTo(50);
    }

    @Test
    void earn_withExistingAccount_addsToBalance() {
        LoyaltyAccount account = LoyaltyAccount.builder().member(member).pointsBalance(100).build();
        when(loyaltyAccountRepository.findByMemberId(1L)).thenReturn(Optional.of(account));

        LoyaltyTransactionResponse response = loyaltyService.earn(new EarnPointsRequest(1L, 25, "Promo"));

        assertThat(response.balanceAfter()).isEqualTo(125);
    }

    @Test
    void redeem_withSufficientPoints_deductsBalance() {
        LoyaltyAccount account = LoyaltyAccount.builder().member(member).pointsBalance(100).build();
        Reward reward = Reward.builder().name("Free Shake").pointsCost(60).active(true).build();
        reward.setId(5L);

        when(loyaltyAccountRepository.findByMemberId(1L)).thenReturn(Optional.of(account));
        when(rewardRepository.findById(5L)).thenReturn(Optional.of(reward));

        LoyaltyTransactionResponse response = loyaltyService.redeem(1L, 5L);

        assertThat(response.balanceAfter()).isEqualTo(40);
        assertThat(response.rewardId()).isEqualTo(5L);
    }

    @Test
    void redeem_withInsufficientPoints_throwsBadRequest() {
        LoyaltyAccount account = LoyaltyAccount.builder().member(member).pointsBalance(10).build();
        Reward reward = Reward.builder().name("Free Shake").pointsCost(60).active(true).build();
        reward.setId(5L);

        when(loyaltyAccountRepository.findByMemberId(1L)).thenReturn(Optional.of(account));
        when(rewardRepository.findById(5L)).thenReturn(Optional.of(reward));

        assertThatThrownBy(() -> loyaltyService.redeem(1L, 5L))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void redeem_withOutOfStockReward_throwsBadRequest() {
        LoyaltyAccount account = LoyaltyAccount.builder().member(member).pointsBalance(1000).build();
        Reward reward = Reward.builder().name("Limited Shirt").pointsCost(60).active(true).stockQuantity(0).build();
        reward.setId(6L);

        when(loyaltyAccountRepository.findByMemberId(1L)).thenReturn(Optional.of(account));
        when(rewardRepository.findById(6L)).thenReturn(Optional.of(reward));

        assertThatThrownBy(() -> loyaltyService.redeem(1L, 6L))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void redeem_withInactiveReward_throwsBadRequest() {
        LoyaltyAccount account = LoyaltyAccount.builder().member(member).pointsBalance(1000).build();
        Reward reward = Reward.builder().name("Old Reward").pointsCost(60).active(false).build();
        reward.setId(7L);

        when(loyaltyAccountRepository.findByMemberId(1L)).thenReturn(Optional.of(account));
        when(rewardRepository.findById(7L)).thenReturn(Optional.of(reward));

        assertThatThrownBy(() -> loyaltyService.redeem(1L, 7L))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getBalance_returnsCurrentBalance() {
        LoyaltyAccount account = LoyaltyAccount.builder().member(member).pointsBalance(42).build();
        when(loyaltyAccountRepository.findByMemberId(1L)).thenReturn(Optional.of(account));

        LoyaltyAccountResponse response = loyaltyService.getBalance(1L);

        assertThat(response.pointsBalance()).isEqualTo(42);
    }
}
