package za.co.gymhubsa.api.member.dto;

import jakarta.validation.constraints.NotBlank;
import za.co.gymhubsa.api.member.entity.Gender;

import java.time.LocalDate;

public record MemberUpdateRequest(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        String phoneNumber,
        LocalDate dateOfBirth,
        Gender gender,
        String address,
        String emergencyContactName,
        String emergencyContactPhone
) {
}
