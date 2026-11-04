package za.co.gymhubsa.api.diet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * One meal within a {@link DietPlan} template (e.g. "Breakfast: oats + eggs, ~450 kcal").
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "diet_meals")
public class DietMeal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diet_plan_id", nullable = false)
    private DietPlan dietPlan;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    private Integer calories;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
