package za.co.gymhubsa.api.payment.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Dev/MVP stub standing in for a real payment gateway integration. Simulates
 * initiating a payment by generating a fake reference and redirect URL;
 * actual confirmation is simulated via the webhook endpoint
 * ({@code POST /api/v1/payments/webhook/confirm}) rather than a real
 * provider callback.
 */
@Slf4j
@Service
public class MockPaymentGatewayService implements PaymentGatewayService {

    @Override
    public PaymentInitiationResult initiate(BigDecimal amount, String reference, String description) {
        String gatewayReference = "MOCK-" + UUID.randomUUID();
        String redirectUrl = "https://mock-payment-gateway.local/pay/" + gatewayReference;

        log.info("Initiated mock online payment: reference={}, amount={}, redirectUrl={}",
                reference, amount, redirectUrl);

        return new PaymentInitiationResult(gatewayReference, redirectUrl);
    }
}
