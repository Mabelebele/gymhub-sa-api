package za.co.gymhubsa.api.diet.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;
import za.co.gymhubsa.api.auth.repository.UserRepository;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.entity.MemberStatus;
import za.co.gymhubsa.api.member.repository.MemberRepository;
import za.co.gymhubsa.api.diet.entity.WeightProgressLog;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class WeightProgressLogRepositoryTest {

    @Autowired
    private WeightProgressLogRepository weightProgressLogRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void findFirstByMemberIdOrderByLogDateDesc_returnsMostRecent() {
        User user = userRepository.save(User.builder()
                .firstName("Tshepo").lastName("Maseko").email("tshepo@example.com")
                .password("hashed").role(Role.MEMBER).enabled(true).build());
        Member member = memberRepository.save(Member.builder()
                .user(user).status(MemberStatus.ACTIVE).joinDate(LocalDate.now()).build());

        weightProgressLogRepository.save(WeightProgressLog.builder()
                .member(member).logDate(LocalDate.now().minusDays(7)).weightKg(new BigDecimal("82.00")).build());
        WeightProgressLog latest = weightProgressLogRepository.save(WeightProgressLog.builder()
                .member(member).logDate(LocalDate.now()).weightKg(new BigDecimal("80.50")).build());

        assertThat(weightProgressLogRepository.findFirstByMemberIdOrderByLogDateDesc(member.getId()))
                .contains(latest);
        assertThat(weightProgressLogRepository.findByMemberIdOrderByLogDateDesc(member.getId(), PageRequest.of(0, 10))
                .getContent()).hasSize(2);
    }
}
