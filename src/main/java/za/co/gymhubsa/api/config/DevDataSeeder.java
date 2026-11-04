package za.co.gymhubsa.api.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;
import za.co.gymhubsa.api.auth.repository.UserRepository;

/**
 * Seeds a default admin account in the {@code dev} profile only, so the API
 * is immediately usable (login/Swagger "Authorize") without manual setup.
 * Never runs against the {@code prod} profile.
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private static final String DEFAULT_ADMIN_EMAIL = "admin@gymhubsa.co.za";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@12345";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmailIgnoreCase(DEFAULT_ADMIN_EMAIL)) {
            return;
        }

        User admin = User.builder()
                .firstName("Gym")
                .lastName("Admin")
                .email(DEFAULT_ADMIN_EMAIL)
                .password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);

        log.info("Seeded default dev admin -> email: {}, password: {}", DEFAULT_ADMIN_EMAIL, DEFAULT_ADMIN_PASSWORD);
    }
}
