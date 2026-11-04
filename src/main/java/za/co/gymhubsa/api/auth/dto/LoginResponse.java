package za.co.gymhubsa.api.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserSummaryResponse user
) {
    public static LoginResponse of(String accessToken, String refreshToken, long expiresInMs, UserSummaryResponse user) {
        return new LoginResponse(accessToken, refreshToken, "Bearer", expiresInMs / 1000, user);
    }
}
