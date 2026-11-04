package za.co.gymhubsa.api.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import za.co.gymhubsa.api.common.dto.ApiResponse;
import za.co.gymhubsa.api.member.dto.MemberRegistrationRequest;
import za.co.gymhubsa.api.member.dto.MemberResponse;
import za.co.gymhubsa.api.member.dto.MemberStatusUpdateRequest;
import za.co.gymhubsa.api.member.dto.MemberUpdateRequest;
import za.co.gymhubsa.api.member.entity.MemberStatus;
import za.co.gymhubsa.api.member.service.MemberService;

@Tag(name = "Members", description = "Member registration, profile management, search and status")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "Register a new member (creates both the login account and the member profile)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> register(@Valid @RequestBody MemberRegistrationRequest request) {
        MemberResponse response = memberService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Member registered successfully", response));
    }

    @Operation(summary = "Search/list members with optional keyword and status filters, paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MemberResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) MemberStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MemberResponse> response = memberService.search(keyword, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get the signed-in member's own profile")
    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> getMyProfile(Authentication authentication) {
        MemberResponse response = memberService.getByEmail(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "View a member's profile by id")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> getById(@PathVariable Long id) {
        MemberResponse response = memberService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Update a member's profile details")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> update(@PathVariable Long id,
                                                               @Valid @RequestBody MemberUpdateRequest request) {
        MemberResponse response = memberService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Member updated successfully", response));
    }

    @Operation(summary = "Change a member's status (active/inactive/suspended)")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<MemberResponse>> updateStatus(@PathVariable Long id,
                                                                     @Valid @RequestBody MemberStatusUpdateRequest request) {
        MemberResponse response = memberService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Member status updated successfully", response));
    }

    @Operation(summary = "Deactivate a member (soft delete: disables login, preserves history)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        memberService.delete(id);
        return ResponseEntity.ok(ApiResponse.message("Member deactivated successfully"));
    }

    @Operation(summary = "Upload/replace a member's profile photo")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MemberResponse>> uploadPhoto(@PathVariable Long id,
                                                                    @RequestPart("file") MultipartFile file) {
        MemberResponse response = memberService.uploadProfilePhoto(id, file);
        return ResponseEntity.ok(ApiResponse.success("Profile photo uploaded successfully", response));
    }
}
