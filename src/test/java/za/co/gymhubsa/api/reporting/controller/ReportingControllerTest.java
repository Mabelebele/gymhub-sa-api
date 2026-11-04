package za.co.gymhubsa.api.reporting.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReportingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getMemberReport_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/reports/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalMembers").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getMembershipReport_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/reports/memberships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activeCount").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAttendanceReport_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/reports/attendance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPaymentReport_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/reports/payments")
                        .param("from", "2026-01-01")
                        .param("to", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.byMethod").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRevenueReport_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/reports/revenue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRevenue").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTrainerReport_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/reports/trainers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.trainers").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getMembershipExpiryReport_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/reports/membership-expiry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getLoyaltyReport_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/reports/loyalty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.topEarners").isArray());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getMemberReport_asMember_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/reports/members"))
                .andExpect(status().isForbidden());
    }
}
