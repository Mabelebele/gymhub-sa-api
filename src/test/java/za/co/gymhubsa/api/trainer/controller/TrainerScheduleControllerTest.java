package za.co.gymhubsa.api.trainer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import za.co.gymhubsa.api.trainer.dto.TrainerRegistrationRequest;
import za.co.gymhubsa.api.trainer.dto.TrainerResponse;
import za.co.gymhubsa.api.trainer.dto.TrainerScheduleRequest;
import za.co.gymhubsa.api.trainer.service.TrainerService;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TrainerScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TrainerService trainerService;

    private TrainerResponse trainer;

    @BeforeEach
    void setUp() {
        trainer = trainerService.register(new TrainerRegistrationRequest("Amogelang", "Pule",
                "amogelang." + System.nanoTime() + "@gymhubsa.co.za", "Password123!", null, "Pilates", "bio", 2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addSlotThenGet_asAdmin_works() throws Exception {
        TrainerScheduleRequest request = new TrainerScheduleRequest(DayOfWeek.TUESDAY,
                LocalTime.of(6, 0), LocalTime.of(9, 0));

        mockMvc.perform(post("/api/v1/trainers/" + trainer.id() + "/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dayOfWeek").value("TUESDAY"));

        mockMvc.perform(get("/api/v1/trainers/" + trainer.id() + "/schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void addMySlot_asOwningTrainer_works() throws Exception {
        TrainerScheduleRequest request = new TrainerScheduleRequest(DayOfWeek.WEDNESDAY,
                LocalTime.of(17, 0), LocalTime.of(19, 0));

        mockMvc.perform(post("/api/v1/trainers/me/schedule")
                        .with(user(trainer.email()).roles("TRAINER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dayOfWeek").value("WEDNESDAY"));
    }
}
