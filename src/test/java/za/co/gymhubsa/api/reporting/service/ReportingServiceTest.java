package za.co.gymhubsa.api.reporting.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import za.co.gymhubsa.api.attendance.dto.DailyAttendanceReportResponse;
import za.co.gymhubsa.api.attendance.service.AttendanceService;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;
import za.co.gymhubsa.api.loyalty.entity.LoyaltyAccount;
import za.co.gymhubsa.api.loyalty.entity.LoyaltyTransactionType;
import za.co.gymhubsa.api.loyalty.repository.LoyaltyAccountRepository;
import za.co.gymhubsa.api.loyalty.repository.LoyaltyTransactionRepository;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.entity.MemberStatus;
import za.co.gymhubsa.api.member.repository.MemberRepository;
import za.co.gymhubsa.api.member.service.MemberService;
import za.co.gymhubsa.api.membership.entity.MembershipPlan;
import za.co.gymhubsa.api.membership.entity.MembershipStatus;
import za.co.gymhubsa.api.membership.repository.MemberMembershipRepository;
import za.co.gymhubsa.api.membership.repository.MembershipPlanRepository;
import za.co.gymhubsa.api.membership.service.MemberMembershipService;
import za.co.gymhubsa.api.payment.entity.PaymentMethod;
import za.co.gymhubsa.api.payment.entity.PaymentStatus;
import za.co.gymhubsa.api.payment.repository.PaymentRepository;
import za.co.gymhubsa.api.payment.repository.projection.PaymentMethodBreakdown;
import za.co.gymhubsa.api.payment.service.PaymentService;
import za.co.gymhubsa.api.reporting.dto.LoyaltyReportResponse;
import za.co.gymhubsa.api.reporting.dto.MemberReportResponse;
import za.co.gymhubsa.api.reporting.dto.MembershipReportResponse;
import za.co.gymhubsa.api.reporting.dto.PaymentReportResponse;
import za.co.gymhubsa.api.trainer.entity.Trainer;
import za.co.gymhubsa.api.trainer.entity.TrainerStatus;
import za.co.gymhubsa.api.trainer.repository.TrainerRepository;
import za.co.gymhubsa.api.trainer.service.TrainerService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MemberService memberService;
    @Mock
    private MemberMembershipRepository memberMembershipRepository;
    @Mock
    private MembershipPlanRepository membershipPlanRepository;
    @Mock
    private MemberMembershipService memberMembershipService;
    @Mock
    private AttendanceService attendanceService;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentService paymentService;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TrainerService trainerService;
    @Mock
    private LoyaltyAccountRepository loyaltyAccountRepository;
    @Mock
    private LoyaltyTransactionRepository loyaltyTransactionRepository;

    private ReportingService reportingService;

    @BeforeEach
    void setUp() {
        reportingService = new ReportingService(memberRepository, memberService, memberMembershipRepository,
                membershipPlanRepository, memberMembershipService, attendanceService, paymentRepository,
                paymentService, trainerRepository, trainerService, loyaltyAccountRepository,
                loyaltyTransactionRepository);
    }

    @Test
    void getMemberReport_returnsStatusCountsAndList() {
        when(memberRepository.count()).thenReturn(100L);
        when(memberRepository.countByStatus(MemberStatus.ACTIVE)).thenReturn(80L);
        when(memberRepository.countByStatus(MemberStatus.INACTIVE)).thenReturn(15L);
        when(memberRepository.countByStatus(MemberStatus.SUSPENDED)).thenReturn(5L);
        when(memberService.search(any(), any(), any())).thenReturn(org.springframework.data.domain.Page.empty());

        MemberReportResponse report = reportingService.getMemberReport(null, null, PageRequest.of(0, 10));

        assertThat(report.totalMembers()).isEqualTo(100L);
        assertThat(report.activeCount()).isEqualTo(80L);
        assertThat(report.inactiveCount()).isEqualTo(15L);
        assertThat(report.suspendedCount()).isEqualTo(5L);
    }

    @Test
    void getMembershipReport_returnsStatusCountsAndPlanDistribution() {
        MembershipPlan plan = MembershipPlan.builder().name("Monthly Basic").durationDays(30)
                .price(new BigDecimal("499.00")).active(true).build();
        plan.setId(1L);

        when(memberMembershipRepository.countByStatus(MembershipStatus.ACTIVE)).thenReturn(50L);
        when(memberMembershipRepository.countByStatus(MembershipStatus.EXPIRED)).thenReturn(20L);
        when(memberMembershipRepository.countByStatus(MembershipStatus.CANCELLED)).thenReturn(5L);
        when(membershipPlanRepository.findByActiveTrue()).thenReturn(List.of(plan));
        when(memberMembershipRepository.countByMembershipPlanIdAndStatus(1L, MembershipStatus.ACTIVE)).thenReturn(30L);

        MembershipReportResponse report = reportingService.getMembershipReport();

        assertThat(report.activeCount()).isEqualTo(50L);
        assertThat(report.planDistribution()).hasSize(1);
        assertThat(report.planDistribution().get(0).activeMemberCount()).isEqualTo(30L);
    }

    @Test
    void getAttendanceReport_delegatesToAttendanceService() {
        LocalDate date = LocalDate.of(2026, 1, 15);
        DailyAttendanceReportResponse expected = new DailyAttendanceReportResponse(date, 10, List.of());
        when(attendanceService.getDailyReport(date)).thenReturn(expected);

        assertThat(reportingService.getAttendanceReport(date)).isEqualTo(expected);
    }

    @Test
    void getPaymentReport_sumsBreakdownAcrossMethods() {
        PaymentMethodBreakdown cashBreakdown = breakdown(PaymentMethod.CASH, new BigDecimal("1000.00"), 2L);
        PaymentMethodBreakdown cardBreakdown = breakdown(PaymentMethod.CARD, new BigDecimal("500.00"), 1L);
        when(paymentRepository.breakdownByMethod(eq(PaymentStatus.COMPLETED), any(), any()))
                .thenReturn(List.of(cashBreakdown, cardBreakdown));

        PaymentReportResponse report = reportingService.getPaymentReport(
                LocalDate.now().minusDays(7), LocalDate.now());

        assertThat(report.totalAmount()).isEqualByComparingTo("1500.00");
        assertThat(report.totalCount()).isEqualTo(3L);
        assertThat(report.byMethod()).hasSize(2);
    }

    @Test
    void getTrainerReport_returnsPerformanceForActiveTrainers() {
        User user = User.builder().firstName("Sifiso").lastName("Khumalo").email("sifiso@example.com")
                .password("hashed").role(Role.TRAINER).enabled(true).build();
        user.setId(1L);
        Trainer trainer = Trainer.builder().user(user).status(TrainerStatus.ACTIVE).hireDate(LocalDate.now()).build();
        trainer.setId(10L);

        when(trainerRepository.findByStatus(TrainerStatus.ACTIVE)).thenReturn(List.of(trainer));
        when(trainerService.getPerformance(10L)).thenReturn(
                new za.co.gymhubsa.api.trainer.dto.TrainerPerformanceResponse(10L, "Sifiso Khumalo", 5, 3, 8));

        var report = reportingService.getTrainerReport();

        assertThat(report.trainers()).hasSize(1);
        assertThat(report.trainers().get(0).trainerId()).isEqualTo(10L);
    }

    @Test
    void getLoyaltyReport_returnsTotalsAndTopEarners() {
        User user = User.builder().firstName("Zanele").lastName("Nkosi").email("zanele@example.com")
                .password("hashed").role(Role.MEMBER).enabled(true).build();
        user.setId(1L);
        Member member = Member.builder().user(user).status(MemberStatus.ACTIVE).joinDate(LocalDate.now()).build();
        member.setId(5L);
        LoyaltyAccount account = LoyaltyAccount.builder().member(member).pointsBalance(300).build();

        when(loyaltyTransactionRepository.sumPointsByType(LoyaltyTransactionType.EARNED)).thenReturn(1000L);
        when(loyaltyTransactionRepository.sumPointsByType(LoyaltyTransactionType.REDEEMED)).thenReturn(400L);
        when(loyaltyAccountRepository.findTop5ByOrderByPointsBalanceDesc()).thenReturn(List.of(account));

        LoyaltyReportResponse report = reportingService.getLoyaltyReport();

        assertThat(report.totalPointsEarned()).isEqualTo(1000L);
        assertThat(report.totalPointsRedeemed()).isEqualTo(400L);
        assertThat(report.topEarners()).hasSize(1);
        assertThat(report.topEarners().get(0).pointsBalance()).isEqualTo(300);
    }

    private PaymentMethodBreakdown breakdown(PaymentMethod method, BigDecimal total, Long count) {
        return new PaymentMethodBreakdown() {
            @Override
            public PaymentMethod getMethod() {
                return method;
            }

            @Override
            public BigDecimal getTotal() {
                return total;
            }

            @Override
            public Long getCount() {
                return count;
            }
        };
    }
}
