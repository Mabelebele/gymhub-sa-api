package za.co.gymhubsa.api.member.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.entity.MemberStatus;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private za.co.gymhubsa.api.auth.repository.UserRepository userRepository;

    @Test
    void search_filtersByKeywordAndStatus() {
        User user = userRepository.save(User.builder()
                .firstName("Sipho")
                .lastName("Dlamini")
                .email("sipho@example.com")
                .password("hashed")
                .role(Role.MEMBER)
                .enabled(true)
                .build());

        memberRepository.save(Member.builder()
                .user(user)
                .status(MemberStatus.ACTIVE)
                .joinDate(LocalDate.now())
                .build());

        var activeResults = memberRepository.search("sipho", MemberStatus.ACTIVE, PageRequest.of(0, 10));
        var inactiveResults = memberRepository.search("sipho", MemberStatus.INACTIVE, PageRequest.of(0, 10));

        assertThat(activeResults.getContent()).hasSize(1);
        assertThat(inactiveResults.getContent()).isEmpty();
    }

    @Test
    void findByUserId_returnsMatchingMember() {
        User user = userRepository.save(User.builder()
                .firstName("Naledi")
                .lastName("Khumalo")
                .email("naledi@example.com")
                .password("hashed")
                .role(Role.MEMBER)
                .enabled(true)
                .build());

        memberRepository.save(Member.builder()
                .user(user)
                .status(MemberStatus.ACTIVE)
                .joinDate(LocalDate.now())
                .build());

        assertThat(memberRepository.findByUserId(user.getId())).isPresent();
        assertThat(memberRepository.existsByUserId(user.getId())).isTrue();
    }
}
