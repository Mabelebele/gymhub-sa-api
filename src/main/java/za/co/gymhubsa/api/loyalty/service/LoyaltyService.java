package za.co.gymhubsa.api.loyalty.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.gymhubsa.api.common.exception.BadRequestException;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.loyalty.dto.EarnPointsRequest;
import za.co.gymhubsa.api.loyalty.dto.LoyaltyAccountResponse;
import za.co.gymhubsa.api.loyalty.dto.LoyaltyTransactionResponse;
import za.co.gymhubsa.api.loyalty.entity.LoyaltyAccount;
import za.co.gymhubsa.api.loyalty.entity.LoyaltyTransaction;
import za.co.gymhubsa.api.loyalty.entity.LoyaltyTransactionType;
import za.co.gymhubsa.api.loyalty.entity.Reward;
import za.co.gymhubsa.api.loyalty.repository.LoyaltyAccountRepository;
import za.co.gymhubsa.api.loyalty.repository.LoyaltyTransactionRepository;
import za.co.gymhubsa.api.loyalty.repository.RewardRepository;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final RewardRepository rewardRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public LoyaltyTransactionResponse earn(EarnPointsRequest request) {
        LoyaltyAccount account = getOrCreateAccount(request.memberId());

        account.setPointsBalance(account.getPointsBalance() + request.points());
        loyaltyAccountRepository.save(account);

        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .member(account.getMember())
                .type(LoyaltyTransactionType.EARNED)
                .points(request.points())
                .reason(request.reason())
                .balanceAfter(account.getPointsBalance())
                .build();
        loyaltyTransactionRepository.save(transaction);

        return LoyaltyTransactionResponse.from(transaction);
    }

    @Transactional
    public LoyaltyTransactionResponse redeem(Long memberId, Long rewardId) {
        LoyaltyAccount account = getOrCreateAccount(memberId);
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> ResourceNotFoundException.of("Reward", rewardId));

        if (!reward.isActive()) {
            throw new BadRequestException("Reward is no longer available");
        }
        if (reward.getStockQuantity() != null && reward.getStockQuantity() <= 0) {
            throw new BadRequestException("Reward is out of stock");
        }
        if (account.getPointsBalance() < reward.getPointsCost()) {
            throw new BadRequestException("Insufficient points balance for this reward");
        }

        account.setPointsBalance(account.getPointsBalance() - reward.getPointsCost());
        loyaltyAccountRepository.save(account);

        if (reward.getStockQuantity() != null) {
            reward.setStockQuantity(reward.getStockQuantity() - 1);
            rewardRepository.save(reward);
        }

        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .member(account.getMember())
                .type(LoyaltyTransactionType.REDEEMED)
                .points(reward.getPointsCost())
                .reason("Redeemed: " + reward.getName())
                .balanceAfter(account.getPointsBalance())
                .reward(reward)
                .build();
        loyaltyTransactionRepository.save(transaction);

        return LoyaltyTransactionResponse.from(transaction);
    }

    @Transactional(readOnly = true)
    public LoyaltyAccountResponse getBalance(Long memberId) {
        return LoyaltyAccountResponse.from(getOrCreateAccount(memberId));
    }

    @Transactional(readOnly = true)
    public Page<LoyaltyTransactionResponse> getHistory(Long memberId, Pageable pageable) {
        return loyaltyTransactionRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable)
                .map(LoyaltyTransactionResponse::from);
    }

    @Transactional
    public LoyaltyTransactionResponse redeemAsMember(String email, Long rewardId) {
        return redeem(resolveMemberIdByEmail(email), rewardId);
    }

    @Transactional(readOnly = true)
    public LoyaltyAccountResponse getMyBalance(String email) {
        return getBalance(resolveMemberIdByEmail(email));
    }

    @Transactional(readOnly = true)
    public Page<LoyaltyTransactionResponse> getMyHistory(String email, Pageable pageable) {
        return getHistory(resolveMemberIdByEmail(email), pageable);
    }

    /** Creates the loyalty account on first use, since members don't start with one at registration. */
    private LoyaltyAccount getOrCreateAccount(Long memberId) {
        return loyaltyAccountRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    Member member = memberRepository.findById(memberId)
                            .orElseThrow(() -> ResourceNotFoundException.of("Member", memberId));
                    LoyaltyAccount newAccount = LoyaltyAccount.builder().member(member).pointsBalance(0).build();
                    return loyaltyAccountRepository.save(newAccount);
                });
    }

    private Long resolveMemberIdByEmail(String email) {
        return memberRepository.findByUser_EmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member profile not found"))
                .getId();
    }
}
