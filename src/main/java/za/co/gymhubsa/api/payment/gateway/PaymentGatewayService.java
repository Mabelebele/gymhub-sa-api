package za.co.gymhubsa.api.payment.gateway;

import java.math.BigDecimal;

/**
 * Abstraction over the online payment gateway. {@link MockPaymentGatewayService}
 * is the dev/MVP stub; swap in a real provider (e.g. PayFast, Paystack,
 * Stripe) later by implementing this interface — no caller code changes.
 */
public interface PaymentGatewayService {

    /**
     * Starts a payment for the given amount/reference and returns a
     * gateway-side reference plus the URL the client should be redirected to
     * in order to complete payment.
     */
    PaymentInitiationResult initiate(BigDecimal amount, String reference, String description);
}
