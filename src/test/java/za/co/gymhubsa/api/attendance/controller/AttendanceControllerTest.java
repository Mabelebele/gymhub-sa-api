package za.co.gymhubsa.api.attendance.controller;

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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberService memberService;

    private MemberResponse member;

    @BeforeEach
    void setUp() {
        member = memberService.register(new MemberRegistrationRequest("Nomsa", "Mthembu",
                "nomsa." + System.nanoTime() + "@gymhubsa.co.za", "Password123!", null, null, null, null, null, null));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void checkInThenCheckOut_fullCycle() throws Exception {
        String checkInBody = objectMapper.writeValueAsString(new za.co.gymhubsa.api.attendance.dto.CheckInRequest(member.id()));

        mockMvc.perform(post("/api/v1/attendance/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkInBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.checkOutTime").doesNotExist());

        // Double check-in should fail
        mockMvc.perform(post("/api/v1/attendance/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkInBody))
                .andExpect(status().isBadRequest());

        String checkOutBody = objectMapper.writeValueAsString(new za.co.gymhubsa.api.attendance.dto.CheckOutRequest(member.id()));
        mockMvc.perform(post("/api/v1/attendance/check-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkOutBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.checkOutTime").exists());

        mockMvc.perform(get("/api/v1/attendance/member/" + member.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));

        // Report is gym-wide (not per-member), so other tests' check-ins on the same
        // day also count — assert at least ours is included, not an exact total.
        mockMvc.perform(get("/api/v1/attendance/daily-report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCheckIns", greaterThanOrEqualTo(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void checkInByQr_withValidCode_returns200() throws Exception {
        String body = objectMapper.writeValueAsString(new za.co.gymhubsa.api.attendance.dto.QrCheckInRequest(member.qrCode()));

        mockMvc.perform(post("/api/v1/attendance/check-in/qr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.method").value("QR_CODE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void checkInByQr_withInvalidCode_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(new za.co.gymhubsa.api.attendance.dto.QrCheckInRequest("not-a-real-code"));

        mockMvc.perform(post("/api/v1/attendance/check-in/qr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
