package za.co.gymhubsa.api.loyalty.controller;

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
import za.co.gymhubsa.api.loyalty.dto.EarnPointsRequest;
import za.co.gymhubsa.api.loyalty.dto.LoyaltyAccountResponse;
import za.co.gymhubsa.api.loyalty.dto.LoyaltyTransactionResponse;
import za.co.gymhubsa.api.loyalty.dto.MemberRedeemRequest;
import za.co.gymhubsa.api.loyalty.dto.RedeemPointsRequest;
import za.co.gymhubsa.api.loyalty.service.LoyaltyService;

@Tag(name = "Loyalty", description = "Earn/redeem loyalty points and view points balance/history")
@RestController
@RequestMapping("/api/v1/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    @Operation(summary = "Award loyalty points to a member (e.g. attendance streak, promotion)")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @PostMapping("/earn")
    public ResponseEntity<ApiResponse<LoyaltyTransactionResponse>> earn(@Valid @RequestBody EarnPointsRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Points awarded successfully", loyaltyService.earn(request)));
    }

    @Operation(summary = "Redeem a member's points for a reward (e.g. front desk fulfilling a redemption)")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @PostMapping("/redeem")
    public ResponseEntity<ApiResponse<LoyaltyTransactionResponse>> redeem(@Valid @RequestBody RedeemPointsRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Points redeemed successfully",
                loyaltyService.redeem(request.memberId(), request.rewardId())));
    }

    @Operation(summary = "Get a member's loyalty points balance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/member/{memberId}/balance")
    public ResponseEntity<ApiResponse<LoyaltyAccountResponse>> getBalance(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(loyaltyService.getBalance(memberId)));
    }

    @Operation(summary = "Get a member's loyalty points transaction history, paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/member/{memberId}/history")
    public ResponseEntity<ApiResponse<Page<LoyaltyTransactionResponse>>> getHistory(
            @PathVariable Long memberId, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(loyaltyService.getHistory(memberId, pageable)));
    }

    @Operation(summary = "Get the signed-in member's own points balance")
    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me/balance")
    public ResponseEntity<ApiResponse<LoyaltyAccountResponse>> getMyBalance(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(loyaltyService.getMyBalance(authentication.getName())));
    }

    @Operation(summary = "Get the signed-in member's own points transaction history")
    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me/history")
    public ResponseEntity<ApiResponse<Page<LoyaltyTransactionResponse>>> getMyHistory(
            Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(loyaltyService.getMyHistory(authentication.getName(), pageable)));
    }

    @Operation(summary = "Redeem the signed-in member's own points for a reward")
    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping("/me/redeem")
    public ResponseEntity<ApiResponse<LoyaltyTransactionResponse>> redeemAsMember(
            Authentication authentication, @Valid @RequestBody MemberRedeemRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Points redeemed successfully",
                loyaltyService.redeemAsMember(authentication.getName(), request.rewardId())));
    }
}
