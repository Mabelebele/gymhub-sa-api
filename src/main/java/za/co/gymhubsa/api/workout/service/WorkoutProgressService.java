package za.co.gymhubsa.api.workout.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.member.repository.MemberRepository;
import za.co.gymhubsa.api.workout.dto.WorkoutProgressLogRequest;
import za.co.gymhubsa.api.workout.dto.WorkoutProgressLogResponse;
import za.co.gymhubsa.api.workout.entity.MemberWorkoutPlan;
import za.co.gymhubsa.api.workout.entity.WorkoutAssignmentStatus;
import za.co.gymhubsa.api.workout.entity.WorkoutProgressLog;
import za.co.gymhubsa.api.workout.repository.MemberWorkoutPlanRepository;
import za.co.gymhubsa.api.workout.repository.WorkoutProgressLogRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class WorkoutProgressService {

    private final WorkoutProgressLogRepository workoutProgressLogRepository;
    private final MemberWorkoutPlanRepository memberWorkoutPlanRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public WorkoutProgressLogResponse addLog(Long memberWorkoutPlanId, WorkoutProgressLogRequest request) {
        MemberWorkoutPlan assignment = findAssignmentOrThrow(memberWorkoutPlanId);

        WorkoutProgressLog log = WorkoutProgressLog.builder()
                .memberWorkoutPlan(assignment)
                .logDate(request.logDate() != null ? request.logDate() : LocalDate.now())
                .durationMinutes(request.durationMinutes())
                .rating(request.rating())
                .notes(request.notes())
                .build();
        workoutProgressLogRepository.save(log);

        return WorkoutProgressLogResponse.from(log);
    }

    @Transactional(readOnly = true)
    public Page<WorkoutProgressLogResponse> getLogs(Long memberWorkoutPlanId, Pageable pageable) {
        return workoutProgressLogRepository.findByMemberWorkoutPlanIdOrderByLogDateDesc(memberWorkoutPlanId, pageable)
                .map(WorkoutProgressLogResponse::from);
    }

    @Transactional
    public WorkoutProgressLogResponse addMyLog(String email, WorkoutProgressLogRequest request) {
        return addLog(resolveMyActiveAssignmentId(email), request);
    }

    @Transactional(readOnly = true)
    public Page<WorkoutProgressLogResponse> getMyLogs(String email, Pageable pageable) {
        return getLogs(resolveMyActiveAssignmentId(email), pageable);
    }

    private Long resolveMyActiveAssignmentId(String email) {
        Long memberId = memberRepository.findByUser_EmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member profile not found"))
                .getId();

        return memberWorkoutPlanRepository
                .findFirstByMemberIdAndStatusOrderByAssignedDateDesc(memberId, WorkoutAssignmentStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("You have no active workout plan"))
                .getId();
    }

    private MemberWorkoutPlan findAssignmentOrThrow(Long id) {
        return memberWorkoutPlanRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Workout plan assignment", id));
    }
}
