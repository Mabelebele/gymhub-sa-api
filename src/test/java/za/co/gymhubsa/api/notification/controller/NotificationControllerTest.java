package za.co.gymhubsa.api.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import za.co.gymhubsa.api.member.dto.MemberRegistrationRequest;
import za.co.gymhubsa.api.member.dto.MemberResponse;
import za.co.gymhubsa.api.member.service.MemberService;
import za.co.gymhubsa.api.notification.dto.SendNotificationRequest;
import za.co.gymhubsa.api.notification.entity.NotificationChannel;
import za.co.gymhubsa.api.notification.entity.NotificationType;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberService memberService;

    private MemberResponse member;

    @BeforeEach
    void setUp() {
        member = memberService.register(new MemberRegistrationRequest("Boipelo", "Seretse",
                "boipelo." + System.nanoTime() + "@gymhubsa.co.za", "Password123!", null, null, null, null, null, null));
    }

    @Test
    void send_asAdmin_returnsSentNotification() throws Exception {
        SendNotificationRequest request = new SendNotificationRequest(member.id(), NotificationChannel.EMAIL,
                NotificationType.GENERAL, "Welcome", "Welcome to GymHub SA!");

        mockMvc.perform(post("/api/v1/notifications/send")
                        .with(user("admin-shim").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SENT"));

        mockMvc.perform(get("/api/v1/notifications/me/history").with(user(member.email()).roles("MEMBER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void send_withoutAuthentication_returns401() throws Exception {
        SendNotificationRequest request = new SendNotificationRequest(member.id(), NotificationChannel.EMAIL,
                NotificationType.GENERAL, "Welcome", "Welcome to GymHub SA!");

        mockMvc.perform(post("/api/v1/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
