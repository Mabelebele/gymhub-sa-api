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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.co.gymhubsa.api.common.dto.ApiResponse;
import za.co.gymhubsa.api.diet.dto.NutritionGoalRequest;
import za.co.gymhubsa.api.diet.dto.NutritionGoalResponse;
import za.co.gymhubsa.api.diet.service.NutritionGoalService;

@Tag(name = "Nutrition Goals", description = "Set and track member nutrition/body-composition goals")
@RestController
@RequestMapping("/api/v1/nutrition-goals")
@RequiredArgsConstructor
public class NutritionGoalController {

    private final NutritionGoalService nutritionGoalService;

    @Operation(summary = "Set a nutrition goal for a member (supersedes their current active goal, if any)")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @PostMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<NutritionGoalResponse>> setGoal(
            @PathVariable Long memberId, @Valid @RequestBody NutritionGoalRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Nutrition goal set successfully",
                nutritionGoalService.setGoal(memberId, request)));
    }

    @Operation(summary = "Mark a nutrition goal as achieved")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @PatchMapping("/{goalId}/achieved")
    public ResponseEntity<ApiResponse<NutritionGoalResponse>> markAchieved(@PathVariable Long goalId) {
        return ResponseEntity.ok(ApiResponse.success(nutritionGoalService.markAchieved(goalId)));
    }

    @Operation(summary = "Get a member's current active nutrition goal")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/member/{memberId}/current")
    public ResponseEntity<ApiResponse<NutritionGoalResponse>> getCurrent(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(nutritionGoalService.getCurrentForMember(memberId)));
    }

    @Operation(summary = "Get a member's nutrition goal history, paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/member/{memberId}/history")
    public ResponseEntity<ApiResponse<Page<NutritionGoalResponse>>> getHistory(
            @PathVariable Long memberId, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(nutritionGoalService.getHistory(memberId, pageable)));
    }

    @Operation(summary = "Get the signed-in member's own current nutrition goal")
    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me/current")
    public ResponseEntity<ApiResponse<NutritionGoalResponse>> getMyCurrent(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(nutritionGoalService.getMyCurrent(authentication.getName())));
    }

    @Operation(summary = "Get the signed-in member's own nutrition goal history")
    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me/history")
    public ResponseEntity<ApiResponse<Page<NutritionGoalResponse>>> getMyHistory(
            Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(nutritionGoalService.getMyHistory(authentication.getName(), pageable)));
    }
}
