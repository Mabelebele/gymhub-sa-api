package za.co.gymhubsa.api.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.co.gymhubsa.api.common.entity.BaseEntity;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.membership.entity.MemberMembership;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A single payment by a member — cash/card/EFT recorded at the front desk
 * (immediately COMPLETED), or an online payment (PENDING until the gateway
 * confirms it via webhook). Optionally linked to the {@link MemberMembership}
 * it pays for, which drives outstanding-balance tracking.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_membership_id")
    private MemberMembership memberMembership;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "receipt_number", unique = true, length = 50)
    private String receiptNumber;

    @Column(name = "gateway_reference", length = 100)
    private String gatewayReference;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(length = 255)
    private String notes;
}
