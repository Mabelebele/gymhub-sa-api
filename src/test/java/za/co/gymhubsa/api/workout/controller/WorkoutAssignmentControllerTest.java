package za.co.gymhubsa.api.workout.controller;

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
import za.co.gymhubsa.api.workout.dto.AssignWorkoutPlanRequest;
import za.co.gymhubsa.api.workout.dto.WorkoutExerciseRequest;
import za.co.gymhubsa.api.workout.dto.WorkoutPlanRequest;
import za.co.gymhubsa.api.workout.dto.WorkoutPlanResponse;
import za.co.gymhubsa.api.workout.dto.WorkoutProgressLogRequest;
import za.co.gymhubsa.api.workout.entity.DifficultyLevel;
import za.co.gymhubsa.api.workout.service.WorkoutPlanService;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WorkoutAssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberService memberService;
    @Autowired
    private WorkoutPlanService workoutPlanService;

    private MemberResponse member;
    private WorkoutPlanResponse plan;

    @BeforeEach
    void setUp() {
        member = memberService.register(new MemberRegistrationRequest("Kagiso", "Modise",
                "kagiso." + System.nanoTime() + "@gymhubsa.co.za", "Password123!", null, null, null, null, null, null));

        plan = workoutPlanService.create(new WorkoutPlanRequest("Strength Plan " + System.nanoTime(), "desc",
                DifficultyLevel.INTERMEDIATE, 6,
                List.of(new WorkoutExerciseRequest("Deadlift", 4, 6, 120, 1, null))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignThenGetCurrentAndHistory_asAdmin() throws Exception {
        AssignWorkoutPlanRequest request = new AssignWorkoutPlanRequest(member.id(), plan.id());

        mockMvc.perform(post("/api/v1/workouts/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mockMvc.perform(get("/api/v1/workouts/member/" + member.id() + "/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.workoutPlanId").value(plan.id()));

        mockMvc.perform(get("/api/v1/workouts/member/" + member.id() + "/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void memberSelfService_currentHistoryAndProgress() throws Exception {
        AssignWorkoutPlanRequest assignRequest = new AssignWorkoutPlanRequest(member.id(), plan.id());
        mockMvc.perform(post("/api/v1/workouts/assign")
                        .with(user("admin-shim").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/workouts/me/current").with(user(member.email()).roles("MEMBER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.workoutPlanId").value(plan.id()));

        WorkoutProgressLogRequest logRequest = new WorkoutProgressLogRequest(null, 40, 3, "Good session");
        mockMvc.perform(post("/api/v1/workouts/me/progress")
                        .with(user(member.email()).roles("MEMBER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.durationMinutes").value(40));

        mockMvc.perform(get("/api/v1/workouts/me/progress").with(user(member.email()).roles("MEMBER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));

        mockMvc.perform(get("/api/v1/workouts/me/history").with(user(member.email()).roles("MEMBER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }
}
