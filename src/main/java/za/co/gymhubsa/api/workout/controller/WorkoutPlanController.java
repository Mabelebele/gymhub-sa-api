package za.co.gymhubsa.api.workout.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import za.co.gymhubsa.api.common.dto.ApiResponse;
import za.co.gymhubsa.api.workout.dto.WorkoutPlanRequest;
import za.co.gymhubsa.api.workout.dto.WorkoutPlanResponse;
import za.co.gymhubsa.api.workout.service.WorkoutPlanService;

import java.util.List;

@Tag(name = "Workout Plans", description = "Manage reusable workout plan templates and their exercises")
@RestController
@RequestMapping("/api/v1/workout-plans")
@RequiredArgsConstructor
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    @Operation(summary = "Create a workout plan template with its exercises")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @PostMapping
    public ResponseEntity<ApiResponse<WorkoutPlanResponse>> create(@Valid @RequestBody WorkoutPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Workout plan created successfully", workoutPlanService.create(request)));
    }

    @Operation(summary = "List workout plan templates (active only by default)")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'MEMBER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkoutPlanResponse>>> list(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(ApiResponse.success(workoutPlanService.list(includeInactive)));
    }

    @Operation(summary = "Get a workout plan template with its full exercise list")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'MEMBER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkoutPlanResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(workoutPlanService.getById(id)));
    }

    @Operation(summary = "Update a workout plan template and replace its exercises")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkoutPlanResponse>> update(@PathVariable Long id,
                                                                    @Valid @RequestBody WorkoutPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Workout plan updated successfully",
                workoutPlanService.update(id, request)));
    }

    @Operation(summary = "Deactivate a workout plan template (soft delete; preserves history)")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        workoutPlanService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.message("Workout plan deactivated successfully"));
    }
}
