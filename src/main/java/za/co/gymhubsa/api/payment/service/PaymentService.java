package za.co.gymhubsa.api.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.gymhubsa.api.common.exception.BadRequestException;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.repository.MemberRepository;
import za.co.gymhubsa.api.membership.entity.MemberMembership;
import za.co.gymhubsa.api.membership.entity.MembershipStatus;
import za.co.gymhubsa.api.membership.repository.MemberMembershipRepository;
import za.co.gymhubsa.api.payment.dto.InitiateOnlinePaymentRequest;
import za.co.gymhubsa.api.payment.dto.OnlinePaymentInitiationResponse;
import za.co.gymhubsa.api.payment.dto.OutstandingBalanceResponse;
import za.co.gymhubsa.api.payment.dto.PaymentResponse;
import za.co.gymhubsa.api.payment.dto.ReceiptResponse;
import za.co.gymhubsa.api.payment.dto.RecordPaymentRequest;
import za.co.gymhubsa.api.payment.dto.RevenueReportResponse;
import za.co.gymhubsa.api.payment.entity.Payment;
import za.co.gymhubsa.api.payment.entity.PaymentMethod;
import za.co.gymhubsa.api.payment.entity.PaymentStatus;
import za.co.gymhubsa.api.payment.gateway.PaymentGatewayService;
import za.co.gymhubsa.api.payment.gateway.PaymentInitiationResult;
import za.co.gymhubsa.api.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final ZoneId GYM_ZONE = ZoneId.of("Africa/Johannesburg");

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final MemberMembershipRepository memberMembershipRepository;
    private final PaymentGatewayService paymentGatewayService;

    @Transactional
    public PaymentResponse recordManualPayment(RecordPaymentRequest request) {
        if (request.method() == PaymentMethod.ONLINE) {
            throw new BadRequestException("Use the online payment initiation endpoint for ONLINE payments");
        }

        Member member = findMemberOrThrow(request.memberId());
        MemberMembership membership = resolveMembershipOrNull(request.membershipId());

        Payment payment = Payment.builder()
                .member(member)
                .memberMembership(membership)
                .amount(request.amount())
                .method(request.method())
                .status(PaymentStatus.COMPLETED)
                .receiptNumber(generateReceiptNumber())
                .paidAt(Instant.now())
                .notes(request.notes())
                .build();
        paymentRepository.save(payment);

        return PaymentResponse.from(payment);
    }

    @Transactional
    public OnlinePaymentInitiationResponse initiateOnlinePayment(String email, InitiateOnlinePaymentRequest request) {
        Member member = memberRepository.findByUser_EmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member profile not found"));
        MemberMembership membership = resolveMembershipOrNull(request.membershipId());

        Payment payment = Payment.builder()
                .member(member)
                .memberMembership(membership)
                .amount(request.amount())
                .method(PaymentMethod.ONLINE)
                .status(PaymentStatus.PENDING)
                .build();
        paymentRepository.save(payment);

        PaymentInitiationResult result = paymentGatewayService.initiate(request.amount(),
                "PAYMENT-" + payment.getId(), "GymHub SA membership payment");

        payment.setGatewayReference(result.gatewayReference());
        paymentRepository.save(payment);

        return new OnlinePaymentInitiationResponse(payment.getId(), result.gatewayReference(), result.redirectUrl());
    }

    /**
     * Simulates the gateway's webhook callback confirming (or failing) a
     * previously initiated online payment.
     */
    @Transactional
    public PaymentResponse confirmOnlinePayment(String gatewayReference, boolean success) {
        Payment payment = paymentRepository.findByGatewayReference(gatewayReference)
                .orElseThrow(() -> new ResourceNotFoundException("No payment found for this gateway reference"));

        if (success) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPaidAt(Instant.now());
            payment.setReceiptNumber(generateReceiptNumber());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }
        paymentRepository.save(payment);

        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getHistory(Long memberId, Pageable pageable) {
        return paymentRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable)
                .map(PaymentResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getMyHistory(String email, Pageable pageable) {
        Member member = memberRepository.findByUser_EmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member profile not found"));
        return getHistory(member.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public ReceiptResponse getReceipt(Long paymentId) {
        Payment payment = findPaymentOrThrow(paymentId);
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BadRequestException("Receipt is only available for completed payments");
        }
        return ReceiptResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public OutstandingBalanceResponse getOutstandingBalance(Long memberId) {
        Member member = findMemberOrThrow(memberId);
        return outstandingBalanceFor(member,
                memberMembershipRepository.findFirstByMemberIdAndStatusOrderByStartDateDesc(memberId, MembershipStatus.ACTIVE)
                        .orElse(null));
    }

    @Transactional(readOnly = true)
    public List<OutstandingBalanceResponse> getOutstandingBalances() {
        return memberMembershipRepository.findByStatus(MembershipStatus.ACTIVE).stream()
                .map(membership -> outstandingBalanceFor(membership.getMember(), membership))
                .filter(balance -> balance.outstandingAmount().compareTo(BigDecimal.ZERO) > 0)
                .toList();
    }

    @Transactional(readOnly = true)
    public RevenueReportResponse getRevenue(LocalDate from, LocalDate to) {
        Instant start = from.atStartOfDay(GYM_ZONE).toInstant();
        Instant end = to.plusDays(1).atStartOfDay(GYM_ZONE).toInstant();

        BigDecimal total = paymentRepository.sumAmountByStatusAndPaidAtBetween(PaymentStatus.COMPLETED, start, end);
        long count = paymentRepository.countByStatusAndPaidAtBetween(PaymentStatus.COMPLETED, start, end);

        return new RevenueReportResponse(from, to, total, count);
    }

    private OutstandingBalanceResponse outstandingBalanceFor(Member member, MemberMembership activeMembership) {
        if (activeMembership == null) {
            return new OutstandingBalanceResponse(member.getId(), fullName(member), BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal expected = activeMembership.getPricePaid();
        BigDecimal paid = paymentRepository.sumCompletedAmountByMembershipId(activeMembership.getId());
        BigDecimal outstanding = expected.subtract(paid).max(BigDecimal.ZERO);

        return new OutstandingBalanceResponse(member.getId(), fullName(member), expected, paid, outstanding);
    }

    private String fullName(Member member) {
        return member.getUser().getFirstName() + " " + member.getUser().getLastName();
    }

    private String generateReceiptNumber() {
        return "RCPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private MemberMembership resolveMembershipOrNull(Long membershipId) {
        if (membershipId == null) {
            return null;
        }
        return memberMembershipRepository.findById(membershipId)
                .orElseThrow(() -> ResourceNotFoundException.of("Membership", membershipId));
    }

    private Member findMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> ResourceNotFoundException.of("Member", memberId));
    }

    private Payment findPaymentOrThrow(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Payment", paymentId));
    }
}
