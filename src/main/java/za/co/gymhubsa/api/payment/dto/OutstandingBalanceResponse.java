package za.co.gymhubsa.api.payment.dto;

import java.math.BigDecimal;

public record OutstandingBalanceResponse(
        Long memberId,
        String memberFullName,
        BigDecimal expectedAmount,
        BigDecimal paidAmount,
        BigDecimal outstandingAmount
) {
}
