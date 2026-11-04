package za.co.gymhubsa.api.trainer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.co.gymhubsa.api.common.dto.ApiResponse;
import za.co.gymhubsa.api.trainer.dto.TrainerScheduleRequest;
import za.co.gymhubsa.api.trainer.dto.TrainerScheduleResponse;
import za.co.gymhubsa.api.trainer.service.TrainerScheduleService;
import za.co.gymhubsa.api.trainer.service.TrainerService;

import java.util.List;

@Tag(name = "Trainer Schedules", description = "Manage trainer weekly availability slots")
@RestController
@RequiredArgsConstructor
public class TrainerScheduleController {

    private final TrainerScheduleService trainerScheduleService;
    private final TrainerService trainerService;

    @Operation(summary = "Add an availability slot for a trainer")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/v1/trainers/{trainerId}/schedule")
    public ResponseEntity<ApiResponse<TrainerScheduleResponse>> addSlot(
            @PathVariable Long trainerId, @Valid @RequestBody TrainerScheduleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(trainerScheduleService.addSlot(trainerId, request)));
    }

    @Operation(summary = "Get a trainer's weekly availability schedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'MEMBER')")
    @GetMapping("/api/v1/trainers/{trainerId}/schedule")
    public ResponseEntity<ApiResponse<List<TrainerScheduleResponse>>> getSchedule(@PathVariable Long trainerId) {
        return ResponseEntity.ok(ApiResponse.success(trainerScheduleService.getSchedule(trainerId)));
    }

    @Operation(summary = "Remove an availability slot from a trainer's schedule")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/v1/trainers/{trainerId}/schedule/{scheduleId}")
    public ResponseEntity<ApiResponse<Void>> removeSlot(@PathVariable Long trainerId, @PathVariable Long scheduleId) {
        trainerScheduleService.removeSlot(trainerId, scheduleId);
        return ResponseEntity.ok(ApiResponse.message("Schedule slot removed successfully"));
    }

    @Operation(summary = "Add an availability slot to the signed-in trainer's own schedule")
    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping("/api/v1/trainers/me/schedule")
    public ResponseEntity<ApiResponse<TrainerScheduleResponse>> addMySlot(
            Authentication authentication, @Valid @RequestBody TrainerScheduleRequest request) {
        Long trainerId = trainerService.getByEmail(authentication.getName()).id();
        return ResponseEntity.ok(ApiResponse.success(trainerScheduleService.addSlot(trainerId, request)));
    }

    @Operation(summary = "Remove a slot from the signed-in trainer's own schedule")
    @PreAuthorize("hasRole('TRAINER')")
    @DeleteMapping("/api/v1/trainers/me/schedule/{scheduleId}")
    public ResponseEntity<ApiResponse<Void>> removeMySlot(Authentication authentication, @PathVariable Long scheduleId) {
        Long trainerId = trainerService.getByEmail(authentication.getName()).id();
        trainerScheduleService.removeSlot(trainerId, scheduleId);
        return ResponseEntity.ok(ApiResponse.message("Schedule slot removed successfully"));
    }
}
