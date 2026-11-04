package za.co.gymhubsa.api.membership.entity;

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

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One subscription period of a {@link Member} to a {@link MembershipPlan}.
 * A new row is created on assign/renew/upgrade/downgrade, so the full set of
 * rows for a member is its membership history.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "member_memberships")
public class MemberMembership extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_plan_id", nullable = false)
    private MembershipPlan membershipPlan;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "price_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePaid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipStatus status;
}
