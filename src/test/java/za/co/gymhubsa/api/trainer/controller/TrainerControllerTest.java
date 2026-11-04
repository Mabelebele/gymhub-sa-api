package za.co.gymhubsa.api.trainer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import za.co.gymhubsa.api.trainer.dto.TrainerRegistrationRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TrainerControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void register_asAdmin_returns200() throws Exception {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest("Naledi", "Mahlangu",
                "naledi.trainer." + System.nanoTime() + "@gymhubsa.co.za", "Password123!", "0821234567",
                "Cardio", "Experienced cardio coach", 4);

        mockMvc.perform(post("/api/v1/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.specialization").value("Cardio"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void register_asMember_returns403() throws Exception {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest("Blocked", "Trainer",
                "blocked.trainer@gymhubsa.co.za", "Password123!", null, null, null, null);

        mockMvc.perform(post("/api/v1/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void search_asMember_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/trainers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
