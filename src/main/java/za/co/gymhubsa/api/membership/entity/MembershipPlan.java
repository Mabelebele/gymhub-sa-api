package za.co.gymhubsa.api.membership.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.co.gymhubsa.api.common.entity.BaseEntity;

import java.math.BigDecimal;

/**
 * A membership product in the catalog (e.g. "Monthly Basic", "Annual
 * Premium"). Plans are never hard-deleted — see {@link #active} — so that
 * historical {@link MemberMembership} records keep a valid reference.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "membership_plans")
public class MembershipPlan extends BaseEntity {

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
