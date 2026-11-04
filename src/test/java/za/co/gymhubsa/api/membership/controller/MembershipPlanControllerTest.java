package za.co.gymhubsa.api.membership.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import za.co.gymhubsa.api.membership.dto.MembershipPlanRequest;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MembershipPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_asAdmin_returns200() throws Exception {
        MembershipPlanRequest request = new MembershipPlanRequest("Weekly Pass", "7-day access", 7,
                new BigDecimal("149.00"));

        mockMvc.perform(post("/api/v1/membership-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Weekly Pass"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void create_asMember_returns403() throws Exception {
        MembershipPlanRequest request = new MembershipPlanRequest("Blocked Plan", "desc", 30,
                new BigDecimal("99.00"));

        mockMvc.perform(post("/api/v1/membership-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void list_asMember_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/membership-plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
