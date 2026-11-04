package za.co.gymhubsa.api.payment.dto;

import za.co.gymhubsa.api.payment.entity.Payment;
import za.co.gymhubsa.api.payment.entity.PaymentMethod;
import za.co.gymhubsa.api.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long id,
        Long memberId,
        String memberFullName,
        Long membershipId,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status,
        String receiptNumber,
        String gatewayReference,
        Instant paidAt,
        Instant createdAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getMember().getId(),
                payment.getMember().getUser().getFirstName() + " " + payment.getMember().getUser().getLastName(),
                payment.getMemberMembership() != null ? payment.getMemberMembership().getId() : null,
                payment.getAmount(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getReceiptNumber(),
                payment.getGatewayReference(),
                payment.getPaidAt(),
                payment.getCreatedAt()
        );
    }
}
