package za.co.gymhubsa.api.workout.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.co.gymhubsa.api.common.entity.BaseEntity;
import za.co.gymhubsa.api.trainer.entity.Trainer;

import java.util.ArrayList;
import java.util.List;

/**
 * A reusable workout plan template (e.g. "Beginner Full Body") made up of an
 * ordered list of {@link WorkoutExercise}s. Assigning it to a member creates
 * a {@link MemberWorkoutPlan} rather than mutating the template.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "workout_plans")
public class WorkoutPlan extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", length = 20)
    private DifficultyLevel difficultyLevel;

    @Column(name = "duration_weeks")
    private Integer durationWeeks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_trainer_id")
    private Trainer createdByTrainer;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "workoutPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder asc")
    @Builder.Default
    private List<WorkoutExercise> exercises = new ArrayList<>();
}
