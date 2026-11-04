package za.co.gymhubsa.api.diet.controller;

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
import za.co.gymhubsa.api.diet.dto.AssignDietPlanRequest;
import za.co.gymhubsa.api.diet.dto.MemberDietPlanResponse;
import za.co.gymhubsa.api.diet.service.DietAssignmentService;

@Tag(name = "Diet Assignments", description = "Assign diet plans to members and track assignment history")
@RestController
@RequestMapping("/api/v1/diets")
@RequiredArgsConstructor
public class DietAssignmentController {

    private final DietAssignmentService dietAssignmentService;

    @Operation(summary = "Assign a diet plan to a member (replaces/cancels their current active plan, if any)")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<MemberDietPlanResponse>> assign(@Valid @RequestBody AssignDietPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Diet plan assigned successfully", dietAssignmentService.assign(request)));
    }

    @Operation(summary = "Get a member's currently active diet plan")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/member/{memberId}/current")
    public ResponseEntity<ApiResponse<MemberDietPlanResponse>> getCurrent(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(dietAssignmentService.getCurrentForMember(memberId)));
    }

    @Operation(summary = "Get a member's diet plan assignment history, paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/member/{memberId}/history")
    public ResponseEntity<ApiResponse<Page<MemberDietPlanResponse>>> getHistory(
            @PathVariable Long memberId, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(dietAssignmentService.getHistory(memberId, pageable)));
    }

    @Operation(summary = "Get the signed-in member's own currently active diet plan")
    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me/current")
    public ResponseEntity<ApiResponse<MemberDietPlanResponse>> getMyCurrent(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(dietAssignmentService.getMyCurrent(authentication.getName())));
    }

    @Operation(summary = "Get the signed-in member's own diet plan history")
    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me/history")
    public ResponseEntity<ApiResponse<Page<MemberDietPlanResponse>>> getMyHistory(
            Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(dietAssignmentService.getMyHistory(authentication.getName(), pageable)));
    }
}
