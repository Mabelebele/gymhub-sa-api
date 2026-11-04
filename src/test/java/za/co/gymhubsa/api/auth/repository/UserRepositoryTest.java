package za.co.gymhubsa.api.auth.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmailIgnoreCase_matchesRegardlessOfCase() {
        User user = User.builder()
                .firstName("Thabo")
                .lastName("Nkosi")
                .email("Thabo.Nkosi@example.com")
                .password("hashed")
                .role(Role.TRAINER)
                .enabled(true)
                .build();
        userRepository.save(user);

        assertThat(userRepository.findByEmailIgnoreCase("thabo.nkosi@example.com")).isPresent();
        assertThat(userRepository.existsByEmailIgnoreCase("THABO.NKOSI@EXAMPLE.COM")).isTrue();
    }

    @Test
    void existsByEmailIgnoreCase_returnsFalseWhenNoMatch() {
        assertThat(userRepository.existsByEmailIgnoreCase("nobody@example.com")).isFalse();
    }
}
