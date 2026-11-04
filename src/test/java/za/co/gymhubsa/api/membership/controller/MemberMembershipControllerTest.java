package za.co.gymhubsa.api.membership.controller;

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
import za.co.gymhubsa.api.membership.dto.MembershipPlanRequest;
import za.co.gymhubsa.api.membership.service.MembershipPlanService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MemberMembershipControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MembershipPlanService membershipPlanService;

    private MemberResponse member;
    private Long memberId;
    private Long planId;

    @BeforeEach
    void setUp() {
        member = memberService.register(new MemberRegistrationRequest("Thandeka", "Zulu",
                "thandeka." + System.nanoTime() + "@gymhubsa.co.za", "Password123!", null, null, null, null, null, null));
        memberId = member.id();

        var plan = membershipPlanService.create(new MembershipPlanRequest("Test Plan " + System.nanoTime(),
                "desc", 30, new BigDecimal("399.00")));
        planId = plan.id();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignThenRenewThenChangePlan_fullLifecycle() throws Exception {
        AssignMembershipRequest assignRequest = new AssignMembershipRequest(memberId, planId);
        mockMvc.perform(post("/api/v1/memberships/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.endDate").value(LocalDate.now().plusDays(30).toString()));

        // Assigning again while already active should fail
        mockMvc.perform(post("/api/v1/memberships/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/memberships/" + memberId + "/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mockMvc.perform(post("/api/v1/memberships/" + memberId + "/renew"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mockMvc.perform(get("/api/v1/memberships/" + memberId + "/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getExpiringSoon_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/memberships/expiring-soon?days=7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void memberSelfService_currentAndHistory() throws Exception {
        AssignMembershipRequest assignRequest = new AssignMembershipRequest(memberId, planId);
        mockMvc.perform(post("/api/v1/memberships/assign")
                        .with(user("admin-shim").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/memberships/me/current").with(user(member.email()).roles("MEMBER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mockMvc.perform(get("/api/v1/memberships/me/history").with(user(member.email()).roles("MEMBER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }
}
