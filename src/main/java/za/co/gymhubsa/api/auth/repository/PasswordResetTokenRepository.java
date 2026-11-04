package za.co.gymhubsa.api.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.gymhubsa.api.auth.entity.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);
}
