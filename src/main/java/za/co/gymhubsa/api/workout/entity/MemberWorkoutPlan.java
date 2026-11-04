package za.co.gymhubsa.api.workout.entity;

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
import za.co.gymhubsa.api.trainer.entity.Trainer;

import java.time.LocalDate;

/**
 * One member's assignment to a {@link WorkoutPlan}. Re-assigning a member to
 * a different plan cancels the previous assignment and creates a new row, so
 * the row history is the member's workout plan history.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "member_workout_plans")
public class MemberWorkoutPlan extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_plan_id", nullable = false)
    private WorkoutPlan workoutPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_trainer_id")
    private Trainer assignedByTrainer;

    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkoutAssignmentStatus status;
}
