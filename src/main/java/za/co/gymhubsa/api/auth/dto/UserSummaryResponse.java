package za.co.gymhubsa.api.auth.dto;

import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;

public record UserSummaryResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Role role
) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getRole());
    }
}
