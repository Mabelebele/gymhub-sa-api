package za.co.gymhubsa.api.loyalty.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.co.gymhubsa.api.common.entity.BaseEntity;

/**
 * A catalog item redeemable with loyalty points. {@link #stockQuantity} is
 * {@code null} for unlimited rewards, or a count decremented on each redemption.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "rewards")
public class Reward extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "points_cost", nullable = false)
    private Integer pointsCost;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
