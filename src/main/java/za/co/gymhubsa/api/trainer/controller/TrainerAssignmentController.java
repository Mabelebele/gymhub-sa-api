package za.co.gymhubsa.api.trainer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import za.co.gymhubsa.api.trainer.dto.AssignTrainerRequest;
import za.co.gymhubsa.api.trainer.dto.MemberTrainerAssignmentResponse;
import za.co.gymhubsa.api.trainer.service.TrainerAssignmentService;
import za.co.gymhubsa.api.trainer.service.TrainerService;

import java.util.List;

@Tag(name = "Trainer Assignments", description = "Assign trainers to members and view current/own assignments")
@RestController
@RequestMapping("/api/v1/trainer-assignments")
@RequiredArgsConstructor
public class TrainerAssignmentController {

    private final TrainerAssignmentService trainerAssignmentService;
    private final TrainerService trainerService;

    @Operation(summary = "Assign a trainer to a member (ends any existing assignment for that member)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<MemberTrainerAssignmentResponse>> assign(@Valid @RequestBody AssignTrainerRequest request) {
        MemberTrainerAssignmentResponse response = trainerAssignmentService.assign(request);
        return ResponseEntity.ok(ApiResponse.success("Trainer assigned successfully", response));
    }

    @Operation(summary = "Get a member's currently assigned trainer")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/member/{memberId}/current")
    public ResponseEntity<ApiResponse<MemberTrainerAssignmentResponse>> getCurrentForMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(trainerAssignmentService.getCurrentForMember(memberId)));
    }

    @Operation(summary = "List members currently assigned to a trainer")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/trainer/{trainerId}/members")
    public ResponseEntity<ApiResponse<List<MemberTrainerAssignmentResponse>>> getMembersForTrainer(@PathVariable Long trainerId) {
        return ResponseEntity.ok(ApiResponse.success(trainerAssignmentService.getMembersForTrainer(trainerId)));
    }

    @Operation(summary = "Get the signed-in trainer's own currently assigned members")
    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/me/members")
    public ResponseEntity<ApiResponse<List<MemberTrainerAssignmentResponse>>> getMyMembers(Authentication authentication) {
        Long trainerId = trainerService.getByEmail(authentication.getName()).id();
        return ResponseEntity.ok(ApiResponse.success(trainerAssignmentService.getMembersForTrainer(trainerId)));
    }
}
