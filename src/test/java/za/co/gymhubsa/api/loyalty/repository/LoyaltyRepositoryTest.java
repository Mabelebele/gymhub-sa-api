package za.co.gymhubsa.api.loyalty.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;
import za.co.gymhubsa.api.auth.repository.UserRepository;
import za.co.gymhubsa.api.loyalty.entity.LoyaltyAccount;
import za.co.gymhubsa.api.loyalty.entity.LoyaltyTransaction;
import za.co.gymhubsa.api.loyalty.entity.LoyaltyTransactionType;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.entity.MemberStatus;
import za.co.gymhubsa.api.member.repository.MemberRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LoyaltyRepositoryTest {

    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;
    @Autowired
    private LoyaltyTransactionRepository loyaltyTransactionRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void findByMemberId_andTransactionHistory_work() {
        User user = userRepository.save(User.builder()
                .firstName("Itumeleng").lastName("Nkoana").email("itumeleng@example.com")
                .password("hashed").role(Role.MEMBER).enabled(true).build());
        Member member = memberRepository.save(Member.builder()
                .user(user).status(MemberStatus.ACTIVE).joinDate(LocalDate.now()).build());

        LoyaltyAccount account = loyaltyAccountRepository.save(LoyaltyAccount.builder()
                .member(member).pointsBalance(50).build());

        loyaltyTransactionRepository.save(LoyaltyTransaction.builder()
                .member(member).type(LoyaltyTransactionType.EARNED).points(50)
                .reason("Signup bonus").balanceAfter(50).build());

        assertThat(loyaltyAccountRepository.findByMemberId(member.getId())).contains(account);
        assertThat(loyaltyTransactionRepository.findByMemberIdOrderByCreatedAtDesc(member.getId(), PageRequest.of(0, 10))
                .getContent()).hasSize(1);
    }
}
