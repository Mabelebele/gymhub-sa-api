package za.co.gymhubsa.api.workout.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import za.co.gymhubsa.api.workout.dto.WorkoutExerciseRequest;
import za.co.gymhubsa.api.workout.dto.WorkoutPlanRequest;
import za.co.gymhubsa.api.workout.entity.DifficultyLevel;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WorkoutPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "TRAINER")
    void create_asTrainer_returns200WithExercises() throws Exception {
        WorkoutPlanRequest request = new WorkoutPlanRequest("Beginner Full Body", "desc", DifficultyLevel.BEGINNER, 4,
                List.of(new WorkoutExerciseRequest("Squat", 3, 10, 60, 1, null)));

        mockMvc.perform(post("/api/v1/workout-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exercises.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void create_asMember_returns403() throws Exception {
        WorkoutPlanRequest request = new WorkoutPlanRequest("Blocked Plan", "desc", DifficultyLevel.BEGINNER, 4,
                List.of(new WorkoutExerciseRequest("Squat", 3, 10, 60, 1, null)));

        mockMvc.perform(post("/api/v1/workout-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void list_asMember_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/workout-plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
