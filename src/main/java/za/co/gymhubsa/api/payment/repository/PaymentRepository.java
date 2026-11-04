package za.co.gymhubsa.api.payment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.co.gymhubsa.api.payment.entity.Payment;
import za.co.gymhubsa.api.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Page<Payment> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    Optional<Payment> findByGatewayReference(String gatewayReference);

    @Query("""
            select coalesce(sum(p.amount), 0) from Payment p
            where p.memberMembership.id = :membershipId and p.status = 'COMPLETED'
            """)
    BigDecimal sumCompletedAmountByMembershipId(@Param("membershipId") Long membershipId);

    @Query("""
            select coalesce(sum(p.amount), 0) from Payment p
            where p.status = :status and p.paidAt between :from and :to
            """)
    BigDecimal sumAmountByStatusAndPaidAtBetween(@Param("status") PaymentStatus status,
                                                  @Param("from") Instant from, @Param("to") Instant to);

    long countByStatusAndPaidAtBetween(PaymentStatus status, Instant from, Instant to);
}
