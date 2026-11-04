package za.co.gymhubsa.api.diet.controller;

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
import za.co.gymhubsa.api.diet.dto.DietPlanRequest;
import za.co.gymhubsa.api.diet.dto.DietPlanResponse;
import za.co.gymhubsa.api.diet.service.DietPlanService;

import java.util.List;

@Tag(name = "Diet Plans", description = "Manage reusable diet plan templates and their meals")
@RestController
@RequestMapping("/api/v1/diet-plans")
@RequiredArgsConstructor
public class DietPlanController {

    private final DietPlanService dietPlanService;

    @Operation(summary = "Create a diet plan template with its meals")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @PostMapping
    public ResponseEntity<ApiResponse<DietPlanResponse>> create(@Valid @RequestBody DietPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Diet plan created successfully", dietPlanService.create(request)));
    }

    @Operation(summary = "List diet plan templates (active only by default)")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'MEMBER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DietPlanResponse>>> list(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(ApiResponse.success(dietPlanService.list(includeInactive)));
    }

    @Operation(summary = "Get a diet plan template with its full meal list")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'MEMBER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DietPlanResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(dietPlanService.getById(id)));
    }

    @Operation(summary = "Update a diet plan template and replace its meals")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DietPlanResponse>> update(@PathVariable Long id,
                                                                 @Valid @RequestBody DietPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Diet plan updated successfully", dietPlanService.update(id, request)));
    }

    @Operation(summary = "Deactivate a diet plan template (soft delete; preserves history)")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        dietPlanService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.message("Diet plan deactivated successfully"));
    }
}
