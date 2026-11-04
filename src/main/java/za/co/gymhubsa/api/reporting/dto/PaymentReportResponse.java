package za.co.gymhubsa.api.reporting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PaymentReportResponse(
        LocalDate from,
        LocalDate to,
        BigDecimal totalAmount,
        long totalCount,
        List<PaymentMethodBreakdownEntry> byMethod
) {
}
