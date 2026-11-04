package za.co.gymhubsa.api.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;
import za.co.gymhubsa.api.common.exception.BadRequestException;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.entity.MemberStatus;
import za.co.gymhubsa.api.member.repository.MemberRepository;
import za.co.gymhubsa.api.membership.entity.MemberMembership;
import za.co.gymhubsa.api.membership.entity.MembershipPlan;
import za.co.gymhubsa.api.membership.entity.MembershipStatus;
import za.co.gymhubsa.api.membership.repository.MemberMembershipRepository;
import za.co.gymhubsa.api.payment.dto.InitiateOnlinePaymentRequest;
import za.co.gymhubsa.api.payment.dto.OnlinePaymentInitiationResponse;
import za.co.gymhubsa.api.payment.dto.OutstandingBalanceResponse;
import za.co.gymhubsa.api.payment.dto.PaymentResponse;
import za.co.gymhubsa.api.payment.dto.RecordPaymentRequest;
import za.co.gymhubsa.api.payment.dto.RevenueReportResponse;
import za.co.gymhubsa.api.payment.entity.Payment;
import za.co.gymhubsa.api.payment.entity.PaymentMethod;
import za.co.gymhubsa.api.payment.entity.PaymentStatus;
import za.co.gymhubsa.api.payment.gateway.PaymentGatewayService;
import za.co.gymhubsa.api.payment.gateway.PaymentInitiationResult;
import za.co.gymhubsa.api.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MemberMembershipRepository memberMembershipRepository;
    @Mock
    private PaymentGatewayService paymentGatewayService;

    private PaymentService paymentService;

    private Member member;
    private MemberMembership membership;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, memberRepository, memberMembershipRepository,
                paymentGatewayService);

        User user = User.builder().firstName("Tumi").lastName("Masilela").email("tumi@example.com")
                .password("hashed").role(Role.MEMBER).enabled(true).build();
        user.setId(1L);

        member = Member.builder().user(user).status(MemberStatus.ACTIVE).joinDate(LocalDate.now()).build();
        member.setId(1L);

        MembershipPlan plan = MembershipPlan.builder().name("Monthly Basic").durationDays(30)
                .price(new BigDecimal("499.00")).active(true).build();
        plan.setId(10L);

        membership = MemberMembership.builder().member(member).membershipPlan(plan)
                .startDate(LocalDate.now()).endDate(LocalDate.now().plusDays(30))
                .pricePaid(new BigDecimal("499.00")).status(MembershipStatus.ACTIVE).build();
        membership.setId(100L);
    }

    @Test
    void recordManualPayment_withCashMethod_createsCompletedPayment() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberMembershipRepository.findById(100L)).thenReturn(Optional.of(membership));

        RecordPaymentRequest request = new RecordPaymentRequest(1L, 100L, new BigDecimal("499.00"),
                PaymentMethod.CASH, "Front desk payment");

        PaymentResponse response = paymentService.recordManualPayment(request);

        assertThat(response.status()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(response.receiptNumber()).startsWith("RCPT-");
    }

    @Test
    void recordManualPayment_withOnlineMethod_throwsBadRequest() {
        RecordPaymentRequest request = new RecordPaymentRequest(1L, null, new BigDecimal("499.00"),
                PaymentMethod.ONLINE, null);

        assertThatThrownBy(() -> paymentService.recordManualPayment(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void initiateOnlinePayment_callsGatewayAndStoresReference() {
        when(memberRepository.findByUser_EmailIgnoreCase("tumi@example.com")).thenReturn(Optional.of(member));
        when(paymentGatewayService.initiate(any(), anyString(), anyString()))
                .thenReturn(new PaymentInitiationResult("MOCK-REF-1", "https://mock-payment-gateway.local/pay/1"));

        OnlinePaymentInitiationResponse response = paymentService.initiateOnlinePayment("tumi@example.com",
                new InitiateOnlinePaymentRequest(null, new BigDecimal("499.00")));

        assertThat(response.gatewayReference()).isEqualTo("MOCK-REF-1");
        assertThat(response.redirectUrl()).contains("mock-payment-gateway");
    }

    @Test
    void confirmOnlinePayment_withSuccess_marksCompleted() {
        Payment pendingPayment = Payment.builder().member(member).amount(new BigDecimal("499.00"))
                .method(PaymentMethod.ONLINE).status(PaymentStatus.PENDING).gatewayReference("MOCK-REF-1").build();
        when(paymentRepository.findByGatewayReference("MOCK-REF-1")).thenReturn(Optional.of(pendingPayment));

        PaymentResponse response = paymentService.confirmOnlinePayment("MOCK-REF-1", true);

        assertThat(response.status()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(response.receiptNumber()).isNotNull();
    }

    @Test
    void confirmOnlinePayment_withFailure_marksFailed() {
        Payment pendingPayment = Payment.builder().member(member).amount(new BigDecimal("499.00"))
                .method(PaymentMethod.ONLINE).status(PaymentStatus.PENDING).gatewayReference("MOCK-REF-2").build();
        when(paymentRepository.findByGatewayReference("MOCK-REF-2")).thenReturn(Optional.of(pendingPayment));

        PaymentResponse response = paymentService.confirmOnlinePayment("MOCK-REF-2", false);

        assertThat(response.status()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void getReceipt_forPendingPayment_throwsBadRequest() {
        Payment pendingPayment = Payment.builder().member(member).amount(new BigDecimal("499.00"))
                .method(PaymentMethod.ONLINE).status(PaymentStatus.PENDING).build();
        pendingPayment.setId(5L);
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(pendingPayment));

        assertThatThrownBy(() -> paymentService.getReceipt(5L))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getReceipt_forUnknownPayment_throwsNotFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getReceipt(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getOutstandingBalance_withPartialPayment_returnsRemainder() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberMembershipRepository.findFirstByMemberIdAndStatusOrderByStartDateDesc(1L, MembershipStatus.ACTIVE))
                .thenReturn(Optional.of(membership));
        when(paymentRepository.sumCompletedAmountByMembershipId(100L)).thenReturn(new BigDecimal("200.00"));

        OutstandingBalanceResponse response = paymentService.getOutstandingBalance(1L);

        assertThat(response.outstandingAmount()).isEqualByComparingTo("299.00");
    }

    @Test
    void getOutstandingBalance_withNoActiveMembership_returnsZero() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberMembershipRepository.findFirstByMemberIdAndStatusOrderByStartDateDesc(1L, MembershipStatus.ACTIVE))
                .thenReturn(Optional.empty());

        OutstandingBalanceResponse response = paymentService.getOutstandingBalance(1L);

        assertThat(response.outstandingAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void getOutstandingBalances_excludesFullyPaidMemberships() {
        when(memberMembershipRepository.findByStatus(MembershipStatus.ACTIVE)).thenReturn(List.of(membership));
        when(paymentRepository.sumCompletedAmountByMembershipId(100L)).thenReturn(new BigDecimal("499.00"));

        List<OutstandingBalanceResponse> balances = paymentService.getOutstandingBalances();

        assertThat(balances).isEmpty();
    }

    @Test
    void getRevenue_returnsTotalsFromRepository() {
        when(paymentRepository.sumAmountByStatusAndPaidAtBetween(any(), any(), any()))
                .thenReturn(new BigDecimal("1500.00"));
        when(paymentRepository.countByStatusAndPaidAtBetween(any(), any(), any())).thenReturn(3L);

        RevenueReportResponse response = paymentService.getRevenue(LocalDate.now().minusDays(7), LocalDate.now());

        assertThat(response.totalRevenue()).isEqualByComparingTo("1500.00");
        assertThat(response.paymentCount()).isEqualTo(3);
    }
}
