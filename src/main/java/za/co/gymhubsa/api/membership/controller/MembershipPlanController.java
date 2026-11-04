package za.co.gymhubsa.api.membership.controller;

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
import za.co.gymhubsa.api.membership.dto.MembershipPlanRequest;
import za.co.gymhubsa.api.membership.dto.MembershipPlanResponse;
import za.co.gymhubsa.api.membership.service.MembershipPlanService;

import java.util.List;

@Tag(name = "Membership Plans", description = "Manage the membership plan catalog")
@RestController
@RequestMapping("/api/v1/membership-plans")
@RequiredArgsConstructor
public class MembershipPlanController {

    private final MembershipPlanService membershipPlanService;

    @Operation(summary = "Create a new membership plan")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<MembershipPlanResponse>> create(@Valid @RequestBody MembershipPlanRequest request) {
        MembershipPlanResponse response = membershipPlanService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Membership plan created successfully", response));
    }

    @Operation(summary = "List membership plans (active only by default)")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'MEMBER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<MembershipPlanResponse>>> list(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(ApiResponse.success(membershipPlanService.list(includeInactive)));
    }

    @Operation(summary = "Get a membership plan by id")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'MEMBER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MembershipPlanResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(membershipPlanService.getById(id)));
    }

    @Operation(summary = "Update a membership plan")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MembershipPlanResponse>> update(@PathVariable Long id,
                                                                       @Valid @RequestBody MembershipPlanRequest request) {
        MembershipPlanResponse response = membershipPlanService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Membership plan updated successfully", response));
    }

    @Operation(summary = "Deactivate a membership plan (soft delete; preserves history)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        membershipPlanService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.message("Membership plan deactivated successfully"));
    }
}
