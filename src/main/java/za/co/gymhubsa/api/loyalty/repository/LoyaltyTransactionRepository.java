package za.co.gymhubsa.api.loyalty.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.gymhubsa.api.loyalty.entity.LoyaltyTransaction;

public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {

    Page<LoyaltyTransaction> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
}
