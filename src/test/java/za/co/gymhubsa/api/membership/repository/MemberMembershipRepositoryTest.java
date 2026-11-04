package za.co.gymhubsa.api.membership.repository;

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
import za.co.gymhubsa.api.membership.entity.MemberMembership;
import za.co.gymhubsa.api.membership.entity.MembershipPlan;
import za.co.gymhubsa.api.membership.entity.MembershipStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MemberMembershipRepositoryTest {

    @Autowired
    private MemberMembershipRepository memberMembershipRepository;
    @Autowired
    private MembershipPlanRepository membershipPlanRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void findFirstByMemberIdAndStatus_andExpiringSoonQueries_work() {
        User user = userRepository.save(User.builder()
                .firstName("Kagiso").lastName("Rabada").email("kagiso@example.com")
                .password("hashed").role(Role.MEMBER).enabled(true).build());
        Member member = memberRepository.save(Member.builder()
                .user(user).status(MemberStatus.ACTIVE).joinDate(LocalDate.now()).build());
        MembershipPlan plan = membershipPlanRepository.save(MembershipPlan.builder()
                .name("Monthly Basic").durationDays(30).price(new BigDecimal("499.00")).active(true).build());

        MemberMembership membership = memberMembershipRepository.save(MemberMembership.builder()
                .member(member).membershipPlan(plan)
                .startDate(LocalDate.now()).endDate(LocalDate.now().plusDays(5))
                .pricePaid(plan.getPrice()).status(MembershipStatus.ACTIVE).build());

        assertThat(memberMembershipRepository
                .findFirstByMemberIdAndStatusOrderByStartDateDesc(member.getId(), MembershipStatus.ACTIVE))
                .contains(membership);

        assertThat(memberMembershipRepository
                .findByStatusAndEndDateBetween(MembershipStatus.ACTIVE, LocalDate.now(), LocalDate.now().plusDays(7)))
                .containsExactly(membership);

        assertThat(memberMembershipRepository.findByMemberIdOrderByStartDateDesc(member.getId(), PageRequest.of(0, 10))
                .getContent()).hasSize(1);
    }
}
