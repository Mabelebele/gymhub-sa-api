package za.co.gymhubsa.api.diet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.diet.dto.WeightProgressLogRequest;
import za.co.gymhubsa.api.diet.dto.WeightProgressLogResponse;
import za.co.gymhubsa.api.diet.entity.WeightProgressLog;
import za.co.gymhubsa.api.diet.repository.WeightProgressLogRepository;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.entity.MemberStatus;
import za.co.gymhubsa.api.member.repository.MemberRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeightProgressServiceTest {

    @Mock
    private WeightProgressLogRepository weightProgressLogRepository;
    @Mock
    private MemberRepository memberRepository;

    private WeightProgressService service;

    private Member member;

    @BeforeEach
    void setUp() {
        service = new WeightProgressService(weightProgressLogRepository, memberRepository);

        User user = User.builder().firstName("Lebo").lastName("Ndlovu").email("lebo@example.com")
                .password("hashed").role(Role.MEMBER).enabled(true).build();
        user.setId(1L);
        member = Member.builder().user(user).status(MemberStatus.ACTIVE).joinDate(LocalDate.now()).build();
        member.setId(1L);
    }

    @Test
    void logWeight_withoutExplicitDate_defaultsToToday() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        WeightProgressLogResponse response = service.logWeight(1L,
                new WeightProgressLogRequest(null, new BigDecimal("80.50"), "Morning weigh-in"));

        assertThat(response.logDate()).isEqualTo(LocalDate.now());
        assertThat(response.weightKg()).isEqualByComparingTo("80.50");
    }

    @Test
    void getLatest_whenNoEntries_throwsNotFound() {
        when(weightProgressLogRepository.findFirstByMemberIdOrderByLogDateDesc(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLatest(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getLatest_returnsMostRecentEntry() {
        WeightProgressLog log = WeightProgressLog.builder().member(member).logDate(LocalDate.now())
                .weightKg(new BigDecimal("79.00")).build();
        when(weightProgressLogRepository.findFirstByMemberIdOrderByLogDateDesc(1L)).thenReturn(Optional.of(log));

        WeightProgressLogResponse response = service.getLatest(1L);

        assertThat(response.weightKg()).isEqualByComparingTo("79.00");
    }
}
