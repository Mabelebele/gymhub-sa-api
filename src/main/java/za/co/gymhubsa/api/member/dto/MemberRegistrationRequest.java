package za.co.gymhubsa.api.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import za.co.gymhubsa.api.member.entity.Gender;

import java.time.LocalDate;

public record MemberRegistrationRequest(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        String phoneNumber,
        LocalDate dateOfBirth,
        Gender gender,
        String address,
        String emergencyContactName,
        String emergencyContactPhone
) {
}
