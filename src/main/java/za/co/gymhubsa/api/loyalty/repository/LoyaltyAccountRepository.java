package za.co.gymhubsa.api.loyalty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.gymhubsa.api.loyalty.entity.LoyaltyAccount;

import java.util.Optional;

public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, Long> {

    Optional<LoyaltyAccount> findByMemberId(Long memberId);
}
