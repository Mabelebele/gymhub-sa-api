package za.co.gymhubsa.api.workout.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.co.gymhubsa.api.common.dto.ApiResponse;
import za.co.gymhubsa.api.workout.dto.AssignWorkoutPlanRequest;
import za.co.gymhubsa.api.workout.dto.MemberWorkoutPlanResponse;
import za.co.gymhubsa.api.workout.service.WorkoutAssignmentService;

@Tag(name = "Workout Assignments", description = "Assign workout plans to members and track assignment history")
@RestController
@RequestMapping("/api/v1/workouts")
@RequiredArgsConstructor
public class WorkoutAssignmentController {

    private final WorkoutAssignmentService workoutAssignmentService;

    @Operation(summary = "Assign a workout plan to a member (replaces/cancels their current active plan, if any)")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<MemberWorkoutPlanResponse>> assign(@Valid @RequestBody AssignWorkoutPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Workout plan assigned successfully",
                workoutAssignmentService.assign(request)));
    }

    @Operation(summary = "Mark a member's workout plan assignment as completed")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @PatchMapping("/{memberWorkoutPlanId}/complete")
    public ResponseEntity<ApiResponse<MemberWorkoutPlanResponse>> complete(@PathVariable Long memberWorkoutPlanId) {
        return ResponseEntity.ok(ApiResponse.success(workoutAssignmentService.complete(memberWorkoutPlanId)));
    }

    @Operation(summary = "Get a member's currently active workout plan")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/member/{memberId}/current")
    public ResponseEntity<ApiResponse<MemberWorkoutPlanResponse>> getCurrent(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(workoutAssignmentService.getCurrentForMember(memberId)));
    }

    @Operation(summary = "Get a member's workout plan assignment history, paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/member/{memberId}/history")
    public ResponseEntity<ApiResponse<Page<MemberWorkoutPlanResponse>>> getHistory(
            @PathVariable Long memberId, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(workoutAssignmentService.getHistory(memberId, pageable)));
    }

    @Operation(summary = "Get the signed-in member's own currently active workout plan")
    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me/current")
    public ResponseEntity<ApiResponse<MemberWorkoutPlanResponse>> getMyCurrent(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(workoutAssignmentService.getMyCurrent(authentication.getName())));
    }

    @Operation(summary = "Get the signed-in member's own workout plan history")
    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me/history")
    public ResponseEntity<ApiResponse<Page<MemberWorkoutPlanResponse>>> getMyHistory(
            Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                workoutAssignmentService.getMyHistory(authentication.getName(), pageable)));
    }
}
