package za.co.gymhubsa.api.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import za.co.gymhubsa.api.member.dto.MemberRegistrationRequest;
import za.co.gymhubsa.api.member.dto.MemberResponse;
import za.co.gymhubsa.api.member.service.MemberService;
import za.co.gymhubsa.api.membership.dto.AssignMembershipRequest;
import za.co.gymhubsa.api.membership.dto.MemberMembershipResponse;
import za.co.gymhubsa.api.membership.dto.MembershipPlanRequest;
import za.co.gymhubsa.api.membership.service.MemberMembershipService;
import za.co.gymhubsa.api.membership.service.MembershipPlanService;
import za.co.gymhubsa.api.payment.dto.RecordPaymentRequest;
import za.co.gymhubsa.api.payment.entity.PaymentMethod;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MembershipPlanService membershipPlanService;
    @Autowired
    private MemberMembershipService memberMembershipService;

    private MemberResponse member;
    private MemberMembershipResponse membership;

    @BeforeEach
    void setUp() {
        member = memberService.register(new MemberRegistrationRequest("Boitumelo", "Khoza",
                "boitumelo." + System.nanoTime() + "@gymhubsa.co.za", "Password123!", null, null, null, null, null, null));

        var plan = membershipPlanService.create(new MembershipPlanRequest("Payment Test Plan " + System.nanoTime(),
                "desc", 30, new BigDecimal("499.00")));

        membership = memberMembershipService.assign(new AssignMembershipRequest(member.id(), plan.id()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void recordPayment_thenGetReceiptAndOutstandingBalance() throws Exception {
        RecordPaymentRequest request = new RecordPaymentRequest(member.id(), membership.id(),
                new BigDecimal("200.00"), PaymentMethod.CASH, "Partial payment");

        String responseBody = mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andReturn().getResponse().getContentAsString();

        long paymentId = objectMapper.readTree(responseBody).path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/payments/" + paymentId + "/receipt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.receiptNumber").exists());

        mockMvc.perform(get("/api/v1/payments/member/" + member.id() + "/outstanding-balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.outstandingAmount").value(299.00));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void recordPayment_withOnlineMethod_returns400() throws Exception {
        RecordPaymentRequest request = new RecordPaymentRequest(member.id(), null,
                new BigDecimal("100.00"), PaymentMethod.ONLINE, null);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRevenue_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/payments/revenue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentCount", greaterThanOrEqualTo(0)));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void recordPayment_asMember_returns403() throws Exception {
        RecordPaymentRequest request = new RecordPaymentRequest(member.id(), null,
                new BigDecimal("100.00"), PaymentMethod.CASH, null);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
