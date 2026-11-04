package za.co.gymhubsa.api.payment.dto;

public record OnlinePaymentInitiationResponse(
        Long paymentId,
        String gatewayReference,
        String redirectUrl
) {
}
