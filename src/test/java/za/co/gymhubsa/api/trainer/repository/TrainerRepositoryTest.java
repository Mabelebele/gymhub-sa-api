package za.co.gymhubsa.api.trainer.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;
import za.co.gymhubsa.api.auth.repository.UserRepository;
import za.co.gymhubsa.api.trainer.entity.Trainer;
import za.co.gymhubsa.api.trainer.entity.TrainerStatus;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TrainerRepositoryTest {

    @Autowired
    private TrainerRepository trainerRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void search_filtersByKeywordAndStatus() {
        User user = userRepository.save(User.builder()
                .firstName("Karabo").lastName("Tau").email("karabo@example.com")
                .password("hashed").role(Role.TRAINER).enabled(true).build());

        trainerRepository.save(Trainer.builder()
                .user(user).specialization("CrossFit").status(TrainerStatus.ACTIVE).hireDate(LocalDate.now()).build());

        var activeResults = trainerRepository.search("karabo", TrainerStatus.ACTIVE, PageRequest.of(0, 10));
        var inactiveResults = trainerRepository.search("karabo", TrainerStatus.INACTIVE, PageRequest.of(0, 10));

        assertThat(activeResults.getContent()).hasSize(1);
        assertThat(inactiveResults.getContent()).isEmpty();
    }

    @Test
    void findByUser_EmailIgnoreCase_matchesRegardlessOfCase() {
        User user = userRepository.save(User.builder()
                .firstName("Omphile").lastName("Sekgobela").email("Omphile.S@example.com")
                .password("hashed").role(Role.TRAINER).enabled(true).build());
        trainerRepository.save(Trainer.builder()
                .user(user).status(TrainerStatus.ACTIVE).hireDate(LocalDate.now()).build());

        assertThat(trainerRepository.findByUser_EmailIgnoreCase("omphile.s@example.com")).isPresent();
    }
}
