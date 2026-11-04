package za.co.gymhubsa.api.trainer.controller;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import za.co.gymhubsa.api.common.dto.ApiResponse;
import za.co.gymhubsa.api.trainer.dto.TrainerPerformanceResponse;
import za.co.gymhubsa.api.trainer.dto.TrainerRegistrationRequest;
import za.co.gymhubsa.api.trainer.dto.TrainerResponse;
import za.co.gymhubsa.api.trainer.dto.TrainerStatusUpdateRequest;
import za.co.gymhubsa.api.trainer.dto.TrainerUpdateRequest;
import za.co.gymhubsa.api.trainer.entity.TrainerStatus;
import za.co.gymhubsa.api.trainer.service.TrainerService;

@Tag(name = "Trainers", description = "Trainer registration, profile management, search and performance")
@RestController
@RequestMapping("/api/v1/trainers")
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;

    @Operation(summary = "Register a new trainer (creates both the login account and the trainer profile)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<TrainerResponse>> register(@Valid @RequestBody TrainerRegistrationRequest request) {
        TrainerResponse response = trainerService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Trainer registered successfully", response));
    }

    @Operation(summary = "Search/list trainers with optional keyword and status filters, paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'MEMBER')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TrainerResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TrainerStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(trainerService.search(keyword, status, pageable)));
    }

    @Operation(summary = "Get the signed-in trainer's own profile")
    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<TrainerResponse>> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(trainerService.getByEmail(authentication.getName())));
    }

    @Operation(summary = "View a trainer's profile by id")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'MEMBER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TrainerResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(trainerService.getById(id)));
    }

    @Operation(summary = "Update a trainer's profile details")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TrainerResponse>> update(@PathVariable Long id,
                                                                @Valid @RequestBody TrainerUpdateRequest request) {
        TrainerResponse response = trainerService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Trainer updated successfully", response));
    }

    @Operation(summary = "Change a trainer's status (active/inactive)")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TrainerResponse>> updateStatus(@PathVariable Long id,
                                                                      @Valid @RequestBody TrainerStatusUpdateRequest request) {
        TrainerResponse response = trainerService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Trainer status updated successfully", response));
    }

    @Operation(summary = "Upload/replace a trainer's profile photo")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TrainerResponse>> uploadPhoto(@PathVariable Long id,
                                                                     @RequestPart("file") MultipartFile file) {
        TrainerResponse response = trainerService.uploadProfilePhoto(id, file);
        return ResponseEntity.ok(ApiResponse.success("Profile photo uploaded successfully", response));
    }

    @Operation(summary = "Get a trainer's performance summary (active/total assigned members)")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/{id}/performance")
    public ResponseEntity<ApiResponse<TrainerPerformanceResponse>> getPerformance(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(trainerService.getPerformance(id)));
    }
}
