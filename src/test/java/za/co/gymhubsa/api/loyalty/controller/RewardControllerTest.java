package za.co.gymhubsa.api.loyalty.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import za.co.gymhubsa.api.loyalty.dto.RewardRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RewardControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_asAdmin_returns200() throws Exception {
        RewardRequest request = new RewardRequest("Gym Towel", "Branded towel", 30, 100);

        mockMvc.perform(post("/api/v1/rewards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Gym Towel"));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void create_asMember_returns403() throws Exception {
        RewardRequest request = new RewardRequest("Blocked Reward", "desc", 30, null);

        mockMvc.perform(post("/api/v1/rewards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void list_asMember_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
