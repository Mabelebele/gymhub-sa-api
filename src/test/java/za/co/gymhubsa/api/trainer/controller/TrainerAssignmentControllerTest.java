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
import za.co.gymhubsa.api.member.dto.MemberRegistrationRequest;
import za.co.gymhubsa.api.member.dto.MemberResponse;
import za.co.gymhubsa.api.member.service.MemberService;
import za.co.gymhubsa.api.trainer.dto.AssignTrainerRequest;
import za.co.gymhubsa.api.trainer.dto.TrainerRegistrationRequest;
import za.co.gymhubsa.api.trainer.dto.TrainerResponse;
import za.co.gymhubsa.api.trainer.service.TrainerService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TrainerAssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberService memberService;
    @Autowired
    private TrainerService trainerService;

    private MemberResponse member;
    private TrainerResponse trainer;

    @BeforeEach
    void setUp() {
        member = memberService.register(new MemberRegistrationRequest("Zodwa", "Nxumalo",
                "zodwa." + System.nanoTime() + "@gymhubsa.co.za", "Password123!", null, null, null, null, null, null));
        trainer = trainerService.register(new TrainerRegistrationRequest("Sibusiso", "Radebe",
                "sibusiso." + System.nanoTime() + "@gymhubsa.co.za", "Password123!", null, "Yoga", "bio", 3));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignThenGetCurrent_fullFlow() throws Exception {
        AssignTrainerRequest request = new AssignTrainerRequest(member.id(), trainer.id());

        mockMvc.perform(post("/api/v1/trainer-assignments/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(true));

        mockMvc.perform(get("/api/v1/trainer-assignments/member/" + member.id() + "/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.trainerId").value(trainer.id()));

        mockMvc.perform(get("/api/v1/trainer-assignments/trainer/" + trainer.id() + "/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void getMyMembers_asTrainer_returnsOwnAssignments() throws Exception {
        AssignTrainerRequest assignRequest = new AssignTrainerRequest(member.id(), trainer.id());
        mockMvc.perform(post("/api/v1/trainer-assignments/assign")
                        .with(user("admin-shim").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/trainer-assignments/me/members")
                        .with(user(trainer.email()).roles("TRAINER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }
}
