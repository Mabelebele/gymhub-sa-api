package za.co.gymhubsa.api.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.co.gymhubsa.api.auth.dto.ForgotPasswordRequest;
import za.co.gymhubsa.api.auth.dto.LoginRequest;
import za.co.gymhubsa.api.auth.dto.LoginResponse;
import za.co.gymhubsa.api.auth.dto.RefreshTokenRequest;
import za.co.gymhubsa.api.auth.dto.ResetPasswordRequest;
import za.co.gymhubsa.api.auth.service.AuthService;
import za.co.gymhubsa.api.common.dto.ApiResponse;

@Tag(name = "Auth", description = "Login, logout, token refresh, and password recovery")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Authenticate with email and password, returning an access + refresh token pair")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Exchange a valid refresh token for a new access + refresh token pair")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Log out the current session. Tokens are stateless, so this simply "
            + "clears the server-side security context; clients must discard their tokens.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.message("Logged out successfully"));
    }

    @Operation(summary = "Request a password reset token (sent via email/SMS once notifications are wired up)")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.message(
                "If an account with that email exists, a password reset link has been sent"));
    }

    @Operation(summary = "Reset a password using a valid reset token")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.message("Password reset successfully"));
    }
}
