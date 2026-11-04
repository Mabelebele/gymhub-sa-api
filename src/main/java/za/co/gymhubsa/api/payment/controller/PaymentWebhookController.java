package za.co.gymhubsa.api.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.co.gymhubsa.api.common.dto.ApiResponse;
import za.co.gymhubsa.api.common.exception.UnauthorizedException;
import za.co.gymhubsa.api.payment.dto.PaymentResponse;
import za.co.gymhubsa.api.payment.dto.WebhookConfirmRequest;
import za.co.gymhubsa.api.payment.gateway.PaymentGatewayProperties;
import za.co.gymhubsa.api.payment.service.PaymentService;

/**
 * Simulates the payment gateway's server-to-server webhook callback. Real
 * gateways call this unauthenticated (no user JWT), so it is left out of the
 * normal authenticated API surface and instead verified via a shared secret
 * header — swap for the gateway's real signature verification scheme once a
 * live provider is integrated.
 */
@Tag(name = "Payment Webhook", description = "Gateway callback endpoint (shared-secret authenticated, not JWT)")
@RestController
@RequestMapping("/api/v1/payments/webhook")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private static final String SECRET_HEADER = "X-Webhook-Secret";

    private final PaymentService paymentService;
    private final PaymentGatewayProperties paymentGatewayProperties;

    @Operation(summary = "Gateway webhook confirming or failing a previously initiated online payment")
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirm(
            @RequestHeader(SECRET_HEADER) String secret, @Valid @RequestBody WebhookConfirmRequest request) {
        if (!paymentGatewayProperties.webhookSecret().equals(secret)) {
            throw new UnauthorizedException("Invalid webhook secret");
        }

        PaymentResponse response = paymentService.confirmOnlinePayment(request.gatewayReference(), request.success());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
