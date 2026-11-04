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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.co.gymhubsa.api.common.dto.ApiResponse;
import za.co.gymhubsa.api.workout.dto.WorkoutProgressLogRequest;
import za.co.gymhubsa.api.workout.dto.WorkoutProgressLogResponse;
import za.co.gymhubsa.api.workout.service.WorkoutProgressService;

@Tag(name = "Workout Progress", description = "Log and track progress against an assigned workout plan")
@RestController
@RequestMapping("/api/v1/workouts")
@RequiredArgsConstructor
public class WorkoutProgressController {

    private final WorkoutProgressService workoutProgressService;

    @Operation(summary = "Log progress for a member's workout plan assignment (e.g. trainer logging a session)")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @PostMapping("/{memberWorkoutPlanId}/progress")
    public ResponseEntity<ApiResponse<WorkoutProgressLogResponse>> addLog(
            @PathVariable Long memberWorkoutPlanId, @Valid @RequestBody WorkoutProgressLogRequest request) {
        return ResponseEntity.ok(ApiResponse.success(workoutProgressService.addLog(memberWorkoutPlanId, request)));
    }

    @Operation(summary = "Get progress logs for a member's workout plan assignment, paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/{memberWorkoutPlanId}/progress")
    public ResponseEntity<ApiResponse<Page<WorkoutProgressLogResponse>>> getLogs(
            @PathVariable Long memberWorkoutPlanId, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(workoutProgressService.getLogs(memberWorkoutPlanId, pageable)));
    }

    @Operation(summary = "Log progress against the signed-in member's own active workout plan")
    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping("/me/progress")
    public ResponseEntity<ApiResponse<WorkoutProgressLogResponse>> addMyLog(
            Authentication authentication, @Valid @RequestBody WorkoutProgressLogRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                workoutProgressService.addMyLog(authentication.getName(), request)));
    }

    @Operation(summary = "Get the signed-in member's own workout progress logs, paginated")
    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me/progress")
    public ResponseEntity<ApiResponse<Page<WorkoutProgressLogResponse>>> getMyLogs(
            Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                workoutProgressService.getMyLogs(authentication.getName(), pageable)));
    }
}
