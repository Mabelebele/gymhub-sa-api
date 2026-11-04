package za.co.gymhubsa.api.diet.dto;

import za.co.gymhubsa.api.diet.entity.WeightProgressLog;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WeightProgressLogResponse(
        Long id,
        Long memberId,
        LocalDate logDate,
        BigDecimal weightKg,
        String notes
) {
    public static WeightProgressLogResponse from(WeightProgressLog log) {
        return new WeightProgressLogResponse(log.getId(), log.getMember().getId(), log.getLogDate(),
                log.getWeightKg(), log.getNotes());
    }
}
