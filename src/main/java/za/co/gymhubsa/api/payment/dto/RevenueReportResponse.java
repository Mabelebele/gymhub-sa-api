package za.co.gymhubsa.api.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RevenueReportResponse(
        LocalDate from,
        LocalDate to,
        BigDecimal totalRevenue,
        long paymentCount
) {
}
