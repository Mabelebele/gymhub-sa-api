package za.co.gymhubsa.api.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.gymhubsa.api.auth.dto.ForgotPasswordRequest;
import za.co.gymhubsa.api.auth.dto.LoginRequest;
import za.co.gymhubsa.api.auth.dto.LoginResponse;
import za.co.gymhubsa.api.auth.dto.ResetPasswordRequest;
import za.co.gymhubsa.api.auth.dto.UserSummaryResponse;
import za.co.gymhubsa.api.auth.entity.PasswordResetToken;
import za.co.gymhubsa.api.auth.entity.User;
import za.co.gymhubsa.api.auth.repository.PasswordResetTokenRepository;
import za.co.gymhubsa.api.auth.repository.UserRepository;
import za.co.gymhubsa.api.common.exception.BadRequestException;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.common.exception.UnauthorizedException;
import za.co.gymhubsa.api.security.UserPrincipal;
import za.co.gymhubsa.api.security.jwt.JwtProperties;
import za.co.gymhubsa.api.security.jwt.JwtService;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new UnauthorizedException("Invalid email or password");
        }

        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);

        return LoginResponse.of(accessToken, refreshToken, jwtProperties.accessTokenExpirationMs(),
                UserSummaryResponse.from(user));
    }

    public LoginResponse refresh(String refreshToken) {
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String email = jwtService.extractEmail(refreshToken);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        UserPrincipal principal = new UserPrincipal(user);
        if (!jwtService.isTokenValid(refreshToken, principal)) {
            throw new UnauthorizedException("Refresh token is invalid or expired");
        }

        String newAccessToken = jwtService.generateAccessToken(principal);
        String newRefreshToken = jwtService.generateRefreshToken(principal);

        return LoginResponse.of(newAccessToken, newRefreshToken, jwtProperties.accessTokenExpirationMs(),
                UserSummaryResponse.from(user));
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElse(null);

        // Do not reveal whether the email exists; always respond as if a reset
        // link was sent. Only generate/store a token when the user is real.
        if (user == null) {
            log.info("Forgot-password requested for unknown email: {}", request.email());
            return;
        }

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();
        passwordResetTokenRepository.save(resetToken);

        // TODO: replace with a real call into the Notification module (email/SMS)
        // once it exists. For now we log the token so the flow is testable locally.
        log.info("Password reset token for {}: {}", user.getEmail(), token);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired reset token"));

        if (resetToken.isUsed() || resetToken.isExpired()) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}
