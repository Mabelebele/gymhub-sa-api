package za.co.gymhubsa.api.loyalty.controller;

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
import za.co.gymhubsa.api.loyalty.dto.RewardRequest;
import za.co.gymhubsa.api.loyalty.dto.RewardResponse;
import za.co.gymhubsa.api.loyalty.service.RewardService;

import java.util.List;

@Tag(name = "Rewards", description = "Manage the loyalty rewards catalog")
@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;

    @Operation(summary = "Create a new reward")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<RewardResponse>> create(@Valid @RequestBody RewardRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Reward created successfully", rewardService.create(request)));
    }

    @Operation(summary = "List rewards (active only by default)")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'MEMBER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RewardResponse>>> list(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(ApiResponse.success(rewardService.list(includeInactive)));
    }

    @Operation(summary = "Get a reward by id")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'MEMBER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RewardResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(rewardService.getById(id)));
    }

    @Operation(summary = "Update a reward")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RewardResponse>> update(@PathVariable Long id,
                                                               @Valid @RequestBody RewardRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Reward updated successfully", rewardService.update(id, request)));
    }

    @Operation(summary = "Deactivate a reward (soft delete; preserves redemption history)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        rewardService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.message("Reward deactivated successfully"));
    }
}
