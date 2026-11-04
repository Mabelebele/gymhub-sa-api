package za.co.gymhubsa.api.notification.repository;

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
import za.co.gymhubsa.api.notification.entity.Notification;
import za.co.gymhubsa.api.notification.entity.NotificationChannel;
import za.co.gymhubsa.api.notification.entity.NotificationStatus;
import za.co.gymhubsa.api.notification.entity.NotificationType;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void findByMemberIdOrderByCreatedAtDesc_returnsRecords() {
        User user = userRepository.save(User.builder()
                .firstName("Gugu").lastName("Khumalo").email("gugu@example.com")
                .password("hashed").role(Role.MEMBER).enabled(true).build());
        Member member = memberRepository.save(Member.builder()
                .user(user).status(MemberStatus.ACTIVE).joinDate(LocalDate.now()).build());

        notificationRepository.save(Notification.builder()
                .member(member).recipient("gugu@example.com").channel(NotificationChannel.EMAIL)
                .type(NotificationType.GENERAL).message("Hello").status(NotificationStatus.SENT).build());

        assertThat(notificationRepository.findByMemberIdOrderByCreatedAtDesc(member.getId(), PageRequest.of(0, 10))
                .getContent()).hasSize(1);
    }
}
