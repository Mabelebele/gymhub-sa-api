package za.co.gymhubsa.api.payment.gateway;

public record PaymentInitiationResult(
        String gatewayReference,
        String redirectUrl
) {
}
