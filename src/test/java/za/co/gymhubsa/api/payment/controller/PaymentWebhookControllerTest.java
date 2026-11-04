package za.co.gymhubsa.api.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import za.co.gymhubsa.api.member.dto.MemberRegistrationRequest;
import za.co.gymhubsa.api.member.dto.MemberResponse;
import za.co.gymhubsa.api.member.service.MemberService;
import za.co.gymhubsa.api.payment.dto.InitiateOnlinePaymentRequest;
import za.co.gymhubsa.api.payment.dto.WebhookConfirmRequest;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberService memberService;

    private MemberResponse member;

    @BeforeEach
    void setUp() {
        member = memberService.register(new MemberRegistrationRequest("Onthatile", "Seboka",
                "onthatile." + System.nanoTime() + "@gymhubsa.co.za", "Password123!", null, null, null, null, null, null));
    }

    @Test
    void initiateThenConfirmOnlinePayment_viaWebhook() throws Exception {
        String initiateBody = objectMapper.writeValueAsString(new InitiateOnlinePaymentRequest(null, new BigDecimal("499.00")));
        String initiateResponse = mockMvc.perform(post("/api/v1/payments/online/initiate")
                        .with(user(member.email()).roles("MEMBER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(initiateBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String gatewayReference = objectMapper.readTree(initiateResponse).path("data").path("gatewayReference").asText();

        WebhookConfirmRequest confirmRequest = new WebhookConfirmRequest(gatewayReference, true);
        mockMvc.perform(post("/api/v1/payments/webhook/confirm")
                        .header("X-Webhook-Secret", "dev-only-webhook-secret-change-me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    void confirm_withWrongSecret_returns401() throws Exception {
        WebhookConfirmRequest confirmRequest = new WebhookConfirmRequest("any-ref", true);

        mockMvc.perform(post("/api/v1/payments/webhook/confirm")
                        .header("X-Webhook-Secret", "wrong-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest)))
                .andExpect(status().isUnauthorized());
    }
}
