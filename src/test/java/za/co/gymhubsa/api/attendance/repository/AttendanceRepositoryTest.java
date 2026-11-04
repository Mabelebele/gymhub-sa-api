package za.co.gymhubsa.api.attendance.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import za.co.gymhubsa.api.attendance.entity.Attendance;
import za.co.gymhubsa.api.attendance.entity.CheckInMethod;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;
import za.co.gymhubsa.api.auth.repository.UserRepository;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.entity.MemberStatus;
import za.co.gymhubsa.api.member.repository.MemberRepository;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AttendanceRepositoryTest {

    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void findFirstByMemberIdAndCheckOutTimeIsNull_findsOpenSession() {
        User user = userRepository.save(User.builder()
                .firstName("Palesa").lastName("Moloi").email("palesa@example.com")
                .password("hashed").role(Role.MEMBER).enabled(true).build());
        Member member = memberRepository.save(Member.builder()
                .user(user).status(MemberStatus.ACTIVE).joinDate(LocalDate.now()).build());

        Attendance open = attendanceRepository.save(Attendance.builder()
                .member(member).checkInTime(Instant.now()).method(CheckInMethod.MANUAL).build());

        assertThat(attendanceRepository
                .findFirstByMemberIdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(member.getId()))
                .contains(open);

        assertThat(attendanceRepository.findByMemberIdOrderByCheckInTimeDesc(member.getId(), PageRequest.of(0, 10))
                .getContent()).hasSize(1);
    }
}
