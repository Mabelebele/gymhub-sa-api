package za.co.gymhubsa.api.loyalty.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import za.co.gymhubsa.api.loyalty.dto.EarnPointsRequest;
import za.co.gymhubsa.api.loyalty.dto.MemberRedeemRequest;
import za.co.gymhubsa.api.loyalty.dto.RewardRequest;
import za.co.gymhubsa.api.loyalty.dto.RewardResponse;
import za.co.gymhubsa.api.loyalty.service.RewardService;
import za.co.gymhubsa.api.member.dto.MemberRegistrationRequest;
import za.co.gymhubsa.api.member.dto.MemberResponse;
import za.co.gymhubsa.api.member.service.MemberService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LoyaltyControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberService memberService;
    @Autowired
    private RewardService rewardService;

    private MemberResponse member;
    private RewardResponse reward;

    @BeforeEach
    void setUp() {
        member = memberService.register(new MemberRegistrationRequest("Keabetswe", "Mahlatsi",
                "keabetswe." + System.nanoTime() + "@gymhubsa.co.za", "Password123!", null, null, null, null, null, null));

        reward = rewardService.create(new RewardRequest("Free Shake " + System.nanoTime(), "Protein shake", 50, null));
    }

    @Test
    void earnThenRedeem_fullLifecycle() throws Exception {
        EarnPointsRequest earnRequest = new EarnPointsRequest(member.id(), 100, "Attendance streak");
        mockMvc.perform(post("/api/v1/loyalty/earn")
                        .with(user("admin-shim").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(earnRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balanceAfter").value(100));

        mockMvc.perform(get("/api/v1/loyalty/member/" + member.id() + "/balance")
                        .with(user("admin-shim").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pointsBalance").value(100));

        mockMvc.perform(get("/api/v1/loyalty/me/balance").with(user(member.email()).roles("MEMBER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pointsBalance").value(100));

        MemberRedeemRequest redeemRequest = new MemberRedeemRequest(reward.id());
        mockMvc.perform(post("/api/v1/loyalty/me/redeem")
                        .with(user(member.email()).roles("MEMBER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(redeemRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balanceAfter").value(50));

        mockMvc.perform(get("/api/v1/loyalty/me/history").with(user(member.email()).roles("MEMBER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    void redeem_withInsufficientPoints_returns400() throws Exception {
        MemberRedeemRequest redeemRequest = new MemberRedeemRequest(reward.id());

        mockMvc.perform(post("/api/v1/loyalty/me/redeem")
                        .with(user(member.email()).roles("MEMBER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(redeemRequest)))
                .andExpect(status().isBadRequest());
    }
}
