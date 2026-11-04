package za.co.gymhubsa.api.diet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import za.co.gymhubsa.api.diet.dto.AssignDietPlanRequest;
import za.co.gymhubsa.api.diet.dto.DietMealRequest;
import za.co.gymhubsa.api.diet.dto.DietPlanRequest;
import za.co.gymhubsa.api.diet.dto.DietPlanResponse;
import za.co.gymhubsa.api.diet.dto.NutritionGoalRequest;
import za.co.gymhubsa.api.diet.dto.WeightProgressLogRequest;
import za.co.gymhubsa.api.diet.entity.GoalType;
import za.co.gymhubsa.api.diet.service.DietPlanService;
import za.co.gymhubsa.api.member.dto.MemberRegistrationRequest;
import za.co.gymhubsa.api.member.dto.MemberResponse;
import za.co.gymhubsa.api.member.service.MemberService;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DietNutritionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberService memberService;
    @Autowired
    private DietPlanService dietPlanService;

    private MemberResponse member;
    private DietPlanResponse plan;

    @BeforeEach
    void setUp() {
        member = memberService.register(new MemberRegistrationRequest("Naledi", "Botha",
                "naledi.diet." + System.nanoTime() + "@gymhubsa.co.za", "Password123!", null, null, null, null, null, null));

        plan = dietPlanService.create(new DietPlanRequest("Cutting Plan " + System.nanoTime(), "desc", 1800, 160, 150, 50,
                List.of(new DietMealRequest("Breakfast", "Oats", 400, 1))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPlan_asAdmin_returnsMeals() throws Exception {
        mockMvc.perform(get("/api/v1/diet-plans/" + plan.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meals.length()").value(1));
    }

    @Test
    void assignThenMemberSelfServiceFlow() throws Exception {
        AssignDietPlanRequest assignRequest = new AssignDietPlanRequest(member.id(), plan.id());
        mockMvc.perform(post("/api/v1/diets/assign")
                        .with(user("admin-shim").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mockMvc.perform(get("/api/v1/diets/me/current").with(user(member.email()).roles("MEMBER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dietPlanId").value(plan.id()));

        NutritionGoalRequest goalRequest = new NutritionGoalRequest(GoalType.WEIGHT_LOSS,
                new BigDecimal("85.00"), new BigDecimal("78.00"), null);
        mockMvc.perform(post("/api/v1/nutrition-goals/member/" + member.id())
                        .with(user("admin-shim").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.goalType").value("WEIGHT_LOSS"));

        mockMvc.perform(get("/api/v1/nutrition-goals/me/current").with(user(member.email()).roles("MEMBER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetWeightKg").value(78.00));

        WeightProgressLogRequest weightRequest = new WeightProgressLogRequest(null, new BigDecimal("84.20"), "Week 1");
        mockMvc.perform(post("/api/v1/weight-progress/me")
                        .with(user(member.email()).roles("MEMBER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(weightRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.weightKg").value(84.20));

        mockMvc.perform(get("/api/v1/weight-progress/me/latest").with(user(member.email()).roles("MEMBER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.weightKg").value(84.20));
    }
}
