package za.co.gymhubsa.api.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import za.co.gymhubsa.api.common.dto.ApiResponse;
import za.co.gymhubsa.api.payment.dto.InitiateOnlinePaymentRequest;
import za.co.gymhubsa.api.payment.dto.OnlinePaymentInitiationResponse;
import za.co.gymhubsa.api.payment.dto.OutstandingBalanceResponse;
import za.co.gymhubsa.api.payment.dto.PaymentResponse;
import za.co.gymhubsa.api.payment.dto.ReceiptResponse;
import za.co.gymhubsa.api.payment.dto.RecordPaymentRequest;
import za.co.gymhubsa.api.payment.dto.RevenueReportResponse;
import za.co.gymhubsa.api.payment.service.PaymentService;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Payments", description = "Record payments, receipts, online payments, outstanding balances and revenue")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Record a manual payment (cash/card/EFT) taken at the front desk")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> record(@Valid @RequestBody RecordPaymentRequest request) {
        PaymentResponse response = paymentService.recordManualPayment(request);
        return ResponseEntity.ok(ApiResponse.success("Payment recorded successfully", response));
    }

    @Operation(summary = "Initiate an online payment for the signed-in member; returns a redirect URL")
    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping("/online/initiate")
    public ResponseEntity<ApiResponse<OnlinePaymentInitiationResponse>> initiateOnline(
            Authentication authentication, @Valid @RequestBody InitiateOnlinePaymentRequest request) {
        OnlinePaymentInitiationResponse response =
                paymentService.initiateOnlinePayment(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Online payment initiated", response));
    }

    @Operation(summary = "Get a payment's receipt (only available once the payment is completed)")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/{id}/receipt")
    public ResponseEntity<ApiResponse<ReceiptResponse>> getReceipt(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getReceipt(id)));
    }

    @Operation(summary = "Get a member's payment history, paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getHistory(
            @PathVariable Long memberId, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getHistory(memberId, pageable)));
    }

    @Operation(summary = "Get the signed-in member's own payment history")
    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getMyHistory(
            Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getMyHistory(authentication.getName(), pageable)));
    }

    @Operation(summary = "Get a member's outstanding balance against their active membership")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/member/{memberId}/outstanding-balance")
    public ResponseEntity<ApiResponse<OutstandingBalanceResponse>> getOutstandingBalance(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getOutstandingBalance(memberId)));
    }

    @Operation(summary = "List all members with an outstanding balance")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/outstanding")
    public ResponseEntity<ApiResponse<List<OutstandingBalanceResponse>>> getOutstandingBalances() {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getOutstandingBalances()));
    }

    @Operation(summary = "Get total revenue and payment count for a date range (defaults to current month)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<RevenueReportResponse>> getRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate effectiveTo = to != null ? to : LocalDate.now();
        LocalDate effectiveFrom = from != null ? from : effectiveTo.withDayOfMonth(1);
        return ResponseEntity.ok(ApiResponse.success(paymentService.getRevenue(effectiveFrom, effectiveTo)));
    }
}
