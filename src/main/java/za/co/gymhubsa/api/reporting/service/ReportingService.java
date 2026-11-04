package za.co.gymhubsa.api.reporting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.gymhubsa.api.attendance.dto.DailyAttendanceReportResponse;
import za.co.gymhubsa.api.attendance.service.AttendanceService;
import za.co.gymhubsa.api.loyalty.entity.LoyaltyTransactionType;
import za.co.gymhubsa.api.loyalty.repository.LoyaltyAccountRepository;
import za.co.gymhubsa.api.loyalty.repository.LoyaltyTransactionRepository;
import za.co.gymhubsa.api.member.entity.MemberStatus;
import za.co.gymhubsa.api.member.repository.MemberRepository;
import za.co.gymhubsa.api.member.service.MemberService;
import za.co.gymhubsa.api.membership.dto.MemberMembershipResponse;
import za.co.gymhubsa.api.membership.entity.MembershipStatus;
import za.co.gymhubsa.api.membership.repository.MemberMembershipRepository;
import za.co.gymhubsa.api.membership.repository.MembershipPlanRepository;
import za.co.gymhubsa.api.membership.service.MemberMembershipService;
import za.co.gymhubsa.api.payment.dto.RevenueReportResponse;
import za.co.gymhubsa.api.payment.entity.PaymentStatus;
import za.co.gymhubsa.api.payment.repository.PaymentRepository;
import za.co.gymhubsa.api.payment.service.PaymentService;
import za.co.gymhubsa.api.reporting.dto.LoyaltyReportResponse;
import za.co.gymhubsa.api.reporting.dto.LoyaltyTopEarnerEntry;
import za.co.gymhubsa.api.reporting.dto.MemberReportResponse;
import za.co.gymhubsa.api.reporting.dto.MembershipReportResponse;
import za.co.gymhubsa.api.reporting.dto.PaymentMethodBreakdownEntry;
import za.co.gymhubsa.api.reporting.dto.PaymentReportResponse;
import za.co.gymhubsa.api.reporting.dto.PlanDistributionEntry;
import za.co.gymhubsa.api.reporting.dto.TrainerReportResponse;
import za.co.gymhubsa.api.trainer.entity.TrainerStatus;
import za.co.gymhubsa.api.trainer.repository.TrainerRepository;
import za.co.gymhubsa.api.trainer.service.TrainerService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Read-only reporting layer over Member/Membership/Attendance/Payment/
 * Trainer/Loyalty data. Mirrors Dashboard's aggregation approach but with
 * per-domain detail and date-range filtering, under one consistent
 * {@code /api/v1/reports/*} surface for admins.
 */
@Service
@RequiredArgsConstructor
public class ReportingService {

    private static final ZoneId GYM_ZONE = ZoneId.of("Africa/Johannesburg");
    private static final int DEFAULT_EXPIRY_WINDOW_DAYS = 30;

    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final MemberMembershipRepository memberMembershipRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final MemberMembershipService memberMembershipService;
    private final AttendanceService attendanceService;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final TrainerRepository trainerRepository;
    private final TrainerService trainerService;
    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;

    @Transactional(readOnly = true)
    public MemberReportResponse getMemberReport(String keyword, MemberStatus status, Pageable pageable) {
        return new MemberReportResponse(
                memberRepository.count(),
                memberRepository.countByStatus(MemberStatus.ACTIVE),
                memberRepository.countByStatus(MemberStatus.INACTIVE),
                memberRepository.countByStatus(MemberStatus.SUSPENDED),
                memberService.search(keyword, status, pageable)
        );
    }

    @Transactional(readOnly = true)
    public MembershipReportResponse getMembershipReport() {
        var planDistribution = membershipPlanRepository.findByActiveTrue().stream()
                .map(plan -> new PlanDistributionEntry(plan.getId(), plan.getName(),
                        memberMembershipRepository.countByMembershipPlanIdAndStatus(plan.getId(), MembershipStatus.ACTIVE)))
                .toList();

        return new MembershipReportResponse(
                memberMembershipRepository.countByStatus(MembershipStatus.ACTIVE),
                memberMembershipRepository.countByStatus(MembershipStatus.EXPIRED),
                memberMembershipRepository.countByStatus(MembershipStatus.CANCELLED),
                planDistribution
        );
    }

    @Transactional(readOnly = true)
    public DailyAttendanceReportResponse getAttendanceReport(LocalDate date) {
        return attendanceService.getDailyReport(date != null ? date : LocalDate.now(GYM_ZONE));
    }

    @Transactional(readOnly = true)
    public PaymentReportResponse getPaymentReport(LocalDate from, LocalDate to) {
        Instant start = from.atStartOfDay(GYM_ZONE).toInstant();
        Instant end = to.plusDays(1).atStartOfDay(GYM_ZONE).toInstant();

        var breakdown = paymentRepository.breakdownByMethod(PaymentStatus.COMPLETED, start, end).stream()
                .map(row -> new PaymentMethodBreakdownEntry(row.getMethod(), row.getTotal(), row.getCount()))
                .toList();

        BigDecimalTotal total = breakdown.stream()
                .reduce(new BigDecimalTotal(BigDecimal.ZERO, 0L),
                        (acc, entry) -> new BigDecimalTotal(acc.amount.add(entry.totalAmount()), acc.count + entry.count()),
                        (a, b) -> new BigDecimalTotal(a.amount.add(b.amount), a.count + b.count));

        return new PaymentReportResponse(from, to, total.amount, total.count, breakdown);
    }

    @Transactional(readOnly = true)
    public RevenueReportResponse getRevenueReport(LocalDate from, LocalDate to) {
        return paymentService.getRevenue(from, to);
    }

    @Transactional(readOnly = true)
    public TrainerReportResponse getTrainerReport() {
        var trainers = trainerRepository.findByStatus(TrainerStatus.ACTIVE).stream()
                .map(trainer -> trainerService.getPerformance(trainer.getId()))
                .toList();
        return new TrainerReportResponse(trainers);
    }

    @Transactional(readOnly = true)
    public List<MemberMembershipResponse> getMembershipExpiryReport(Integer days) {
        return memberMembershipService.getExpiringSoon(days != null ? days : DEFAULT_EXPIRY_WINDOW_DAYS);
    }

    @Transactional(readOnly = true)
    public LoyaltyReportResponse getLoyaltyReport() {
        long earned = loyaltyTransactionRepository.sumPointsByType(LoyaltyTransactionType.EARNED);
        long redeemed = loyaltyTransactionRepository.sumPointsByType(LoyaltyTransactionType.REDEEMED);

        var topEarners = loyaltyAccountRepository.findTop5ByOrderByPointsBalanceDesc().stream()
                .map(account -> new LoyaltyTopEarnerEntry(account.getMember().getId(),
                        account.getMember().getUser().getFirstName() + " " + account.getMember().getUser().getLastName(),
                        account.getPointsBalance()))
                .toList();

        return new LoyaltyReportResponse(earned, redeemed, topEarners);
    }

    private record BigDecimalTotal(BigDecimal amount, long count) {
    }
}
