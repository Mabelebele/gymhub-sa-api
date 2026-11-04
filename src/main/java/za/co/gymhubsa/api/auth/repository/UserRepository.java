package za.co.gymhubsa.api.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.gymhubsa.api.auth.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
