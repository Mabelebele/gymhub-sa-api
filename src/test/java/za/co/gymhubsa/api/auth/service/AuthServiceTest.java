package za.co.gymhubsa.api.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import za.co.gymhubsa.api.auth.dto.ForgotPasswordRequest;
import za.co.gymhubsa.api.auth.dto.LoginRequest;
import za.co.gymhubsa.api.auth.dto.LoginResponse;
import za.co.gymhubsa.api.auth.dto.ResetPasswordRequest;
import za.co.gymhubsa.api.auth.entity.PasswordResetToken;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;
import za.co.gymhubsa.api.auth.repository.PasswordResetTokenRepository;
import za.co.gymhubsa.api.auth.repository.UserRepository;
import za.co.gymhubsa.api.common.exception.BadRequestException;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.common.exception.UnauthorizedException;
import za.co.gymhubsa.api.security.jwt.JwtProperties;
import za.co.gymhubsa.api.security.jwt.JwtService;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private Authentication authentication;

    private JwtService jwtService;
    private JwtProperties jwtProperties;

    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties(
                "unit-test-secret-key-unit-test-secret-key-123456", 900_000L, 604_800_000L);
        jwtService = new JwtService(jwtProperties);

        authService = new AuthService(authenticationManager, userRepository, passwordResetTokenRepository,
                passwordEncoder, jwtService, jwtProperties);

        testUser = User.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .password("hashed-password")
                .role(Role.MEMBER)
                .enabled(true)
                .build();
        testUser.setId(1L);
    }

    @Test
    void login_withValidCredentials_returnsTokens() {
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByEmailIgnoreCase("jane@example.com")).thenReturn(Optional.of(testUser));

        LoginResponse response = authService.login(new LoginRequest("jane@example.com", "password123"));

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.user().email()).isEqualTo("jane@example.com");
    }

    @Test
    void login_withInvalidCredentials_throwsUnauthorized() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad creds"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("jane@example.com", "wrong")))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void forgotPassword_forUnknownEmail_doesNotCreateToken() {
        when(userRepository.findByEmailIgnoreCase("ghost@example.com")).thenReturn(Optional.empty());

        authService.forgotPassword(new ForgotPasswordRequest("ghost@example.com"));

        verify(passwordResetTokenRepository, Mockito.never()).save(any());
    }

    @Test
    void forgotPassword_forKnownEmail_createsToken() {
        when(userRepository.findByEmailIgnoreCase("jane@example.com")).thenReturn(Optional.of(testUser));

        authService.forgotPassword(new ForgotPasswordRequest("jane@example.com"));

        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void resetPassword_withExpiredToken_throwsBadRequest() {
        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .token("expired-token")
                .user(testUser)
                .expiryDate(Instant.now().minusSeconds(60))
                .used(false)
                .build();
        when(passwordResetTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("expired-token", "newPassword1")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void resetPassword_withUnknownToken_throwsNotFound() {
        when(passwordResetTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("missing", "newPassword1")))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
