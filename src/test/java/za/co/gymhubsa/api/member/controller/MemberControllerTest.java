package za.co.gymhubsa.api.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import za.co.gymhubsa.api.member.dto.MemberRegistrationRequest;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void register_asAdmin_returns200() throws Exception {
        MemberRegistrationRequest request = new MemberRegistrationRequest("Zanele", "Nkosi",
                "zanele.test@gymhubsa.co.za", "Password123!", "0831234567", LocalDate.of(1998, 3, 15),
                null, "12 Church St", "Mandla Nkosi", "0839998888");

        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("zanele.test@gymhubsa.co.za"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void register_asMember_returns403() throws Exception {
        MemberRegistrationRequest request = new MemberRegistrationRequest("Blocked", "User",
                "blocked@gymhubsa.co.za", "Password123!", null, null, null, null, null, null);

        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void register_withoutAuthentication_returns401() throws Exception {
        MemberRegistrationRequest request = new MemberRegistrationRequest("NoAuth", "User",
                "noauth@gymhubsa.co.za", "Password123!", null, null, null, null, null, null);

        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void search_asAdmin_returnsPagedResults() throws Exception {
        mockMvc.perform(get("/api/v1/members").param("keyword", "a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
