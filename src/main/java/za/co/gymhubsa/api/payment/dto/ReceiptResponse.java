package za.co.gymhubsa.api.payment.dto;

import za.co.gymhubsa.api.payment.entity.Payment;
import za.co.gymhubsa.api.payment.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;

public record ReceiptResponse(
        String receiptNumber,
        Long memberId,
        String memberFullName,
        String memberEmail,
        String planName,
        BigDecimal amount,
        PaymentMethod method,
        Instant paidAt
) {
    public static ReceiptResponse from(Payment payment) {
        return new ReceiptResponse(
                payment.getReceiptNumber(),
                payment.getMember().getId(),
                payment.getMember().getUser().getFirstName() + " " + payment.getMember().getUser().getLastName(),
                payment.getMember().getUser().getEmail(),
                payment.getMemberMembership() != null ? payment.getMemberMembership().getMembershipPlan().getName() : null,
                payment.getAmount(),
                payment.getMethod(),
                payment.getPaidAt()
        );
    }
}
