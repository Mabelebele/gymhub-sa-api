package za.co.gymhubsa.api.trainer.dto;

import za.co.gymhubsa.api.trainer.entity.Trainer;
import za.co.gymhubsa.api.trainer.entity.TrainerStatus;

import java.time.LocalDate;

public record TrainerResponse(
        Long id,
        Long userId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String specialization,
        String bio,
        Integer yearsOfExperience,
        String profilePhotoUrl,
        TrainerStatus status,
        LocalDate hireDate
) {
    public static TrainerResponse from(Trainer trainer) {
        return new TrainerResponse(
                trainer.getId(),
                trainer.getUser().getId(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getUser().getEmail(),
                trainer.getUser().getPhoneNumber(),
                trainer.getSpecialization(),
                trainer.getBio(),
                trainer.getYearsOfExperience(),
                trainer.getProfilePhotoUrl(),
                trainer.getStatus(),
                trainer.getHireDate()
        );
    }
}
