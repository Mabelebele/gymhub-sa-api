package za.co.gymhubsa.api.loyalty.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.co.gymhubsa.api.loyalty.entity.LoyaltyTransaction;
import za.co.gymhubsa.api.loyalty.entity.LoyaltyTransactionType;

public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {

    Page<LoyaltyTransaction> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    @Query("select coalesce(sum(lt.points), 0) from LoyaltyTransaction lt where lt.type = :type")
    long sumPointsByType(@Param("type") LoyaltyTransactionType type);
}
