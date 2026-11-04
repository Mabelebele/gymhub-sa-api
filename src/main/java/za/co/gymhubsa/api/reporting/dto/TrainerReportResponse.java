package za.co.gymhubsa.api.reporting.dto;

import za.co.gymhubsa.api.trainer.dto.TrainerPerformanceResponse;

import java.util.List;

public record TrainerReportResponse(
        List<TrainerPerformanceResponse> trainers
) {
}
