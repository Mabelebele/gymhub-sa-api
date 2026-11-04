package za.co.gymhubsa.api.membership.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import za.co.gymhubsa.api.common.dto.ApiResponse;
import za.co.gymhubsa.api.membership.dto.AssignMembershipRequest;
import za.co.gymhubsa.api.membership.dto.ChangePlanRequest;
import za.co.gymhubsa.api.membership.dto.MemberMembershipResponse;
import za.co.gymhubsa.api.membership.service.MemberMembershipService;

import java.util.List;

@Tag(name = "Memberships", description = "Assign, renew, upgrade/downgrade and track member memberships")
@RestController
@RequestMapping("/api/v1/memberships")
@RequiredArgsConstructor
public class MemberMembershipController {

    private final MemberMembershipService memberMembershipService;

    @Operation(summary = "Assign a membership plan to a member (member must not already have an active one)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<MemberMembershipResponse>> assign(@Valid @RequestBody AssignMembershipRequest request) {
        MemberMembershipResponse response = memberMembershipService.assign(request);
        return ResponseEntity.ok(ApiResponse.success("Membership assigned successfully", response));
    }

    @Operation(summary = "Renew a member's membership (extends from current end date, or starts fresh if already expired)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{memberId}/renew")
    public ResponseEntity<ApiResponse<MemberMembershipResponse>> renew(@PathVariable Long memberId) {
        MemberMembershipResponse response = memberMembershipService.renew(memberId);
        return ResponseEntity.ok(ApiResponse.success("Membership renewed successfully", response));
    }

    @Operation(summary = "Upgrade or downgrade a member's active membership to a different plan")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{memberId}/change-plan")
    public ResponseEntity<ApiResponse<MemberMembershipResponse>> changePlan(@PathVariable Long memberId,
                                                                             @Valid @RequestBody ChangePlanRequest request) {
        MemberMembershipResponse response = memberMembershipService.changePlan(memberId, request);
        return ResponseEntity.ok(ApiResponse.success("Membership plan changed successfully", response));
    }

    @Operation(summary = "Get a member's current active membership")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/{memberId}/current")
    public ResponseEntity<ApiResponse<MemberMembershipResponse>> getCurrent(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(memberMembershipService.getCurrent(memberId)));
    }

    @Operation(summary = "Get a member's membership history, paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/{memberId}/history")
    public ResponseEntity<ApiResponse<Page<MemberMembershipResponse>>> getHistory(
            @PathVariable Long memberId, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(memberMembershipService.getHistory(memberId, pageable)));
    }

    @Operation(summary = "List active memberships expiring within the given number of days (default 7)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/expiring-soon")
    public ResponseEntity<ApiResponse<List<MemberMembershipResponse>>> getExpiringSoon(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(ApiResponse.success(memberMembershipService.getExpiringSoon(days)));
    }
}
