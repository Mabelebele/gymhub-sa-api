package za.co.gymhubsa.api.workout.entity;

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

import java.time.LocalDate;

/**
 * A single progress entry logged against a {@link MemberWorkoutPlan}
 * assignment (e.g. a completed session). The full set of logs for an
 * assignment is its workout progress/history.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "workout_progress_logs")
public class WorkoutProgressLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_workout_plan_id", nullable = false)
    private MemberWorkoutPlan memberWorkoutPlan;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    /** Self-reported difficulty/effort rating, 1 (easy) to 5 (very hard). */
    private Integer rating;

    @Column(length = 500)
    private String notes;
}
