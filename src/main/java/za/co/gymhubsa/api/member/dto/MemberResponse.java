package za.co.gymhubsa.api.member.dto;

import za.co.gymhubsa.api.member.entity.Gender;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.entity.MemberStatus;

import java.time.LocalDate;

public record MemberResponse(
        Long id,
        Long userId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        LocalDate dateOfBirth,
        Gender gender,
        String address,
        String emergencyContactName,
        String emergencyContactPhone,
        String profilePhotoUrl,
        String qrCode,
        MemberStatus status,
        LocalDate joinDate
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getUser().getId(),
                member.getUser().getFirstName(),
                member.getUser().getLastName(),
                member.getUser().getEmail(),
                member.getUser().getPhoneNumber(),
                member.getDateOfBirth(),
                member.getGender(),
                member.getAddress(),
                member.getEmergencyContactName(),
                member.getEmergencyContactPhone(),
                member.getProfilePhotoUrl(),
                member.getQrCode(),
                member.getStatus(),
                member.getJoinDate()
        );
    }
}
