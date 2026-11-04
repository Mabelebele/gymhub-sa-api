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
import za.co.gymhubsa.api.diet.dto.WeightProgressLogRequest;
import za.co.gymhubsa.api.diet.dto.WeightProgressLogResponse;
import za.co.gymhubsa.api.diet.service.WeightProgressService;

@Tag(name = "Weight Progress", description = "Log and track member body weight over time")
@RestController
@RequestMapping("/api/v1/weight-progress")
@RequiredArgsConstructor
public class WeightProgressController {

    private final WeightProgressService weightProgressService;

    @Operation(summary = "Log a weight entry for a member (e.g. trainer recording a weigh-in)")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @PostMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<WeightProgressLogResponse>> logWeight(
            @PathVariable Long memberId, @Valid @RequestBody WeightProgressLogRequest request) {
        return ResponseEntity.ok(ApiResponse.success(weightProgressService.logWeight(memberId, request)));
    }

    @Operation(summary = "Get a member's weight history, paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/member/{memberId}/history")
    public ResponseEntity<ApiResponse<Page<WeightProgressLogResponse>>> getHistory(
            @PathVariable Long memberId, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(weightProgressService.getHistory(memberId, pageable)));
    }

    @Operation(summary = "Get a member's latest logged weight")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/member/{memberId}/latest")
    public ResponseEntity<ApiResponse<WeightProgressLogResponse>> getLatest(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(weightProgressService.getLatest(memberId)));
    }

    @Operation(summary = "Log a weight entry for the signed-in member")
    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping("/me")
    public ResponseEntity<ApiResponse<WeightProgressLogResponse>> logMyWeight(
            Authentication authentication, @Valid @RequestBody WeightProgressLogRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                weightProgressService.logMyWeight(authentication.getName(), request)));
    }

    @Operation(summary = "Get the signed-in member's own weight history")
    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me/history")
    public ResponseEntity<ApiResponse<Page<WeightProgressLogResponse>>> getMyHistory(
            Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                weightProgressService.getMyHistory(authentication.getName(), pageable)));
    }

    @Operation(summary = "Get the signed-in member's own latest logged weight")
    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me/latest")
    public ResponseEntity<ApiResponse<WeightProgressLogResponse>> getMyLatest(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(weightProgressService.getMyLatest(authentication.getName())));
    }
}
