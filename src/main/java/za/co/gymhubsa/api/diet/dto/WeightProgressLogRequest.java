package za.co.gymhubsa.api.diet.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WeightProgressLogRequest(
        LocalDate logDate,

        @NotNull(message = "Weight is required")
        BigDecimal weightKg,

        String notes
) {
}
