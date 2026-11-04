package za.co.gymhubsa.api.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.gymhubsa.api.auth.entity.Role;
import za.co.gymhubsa.api.auth.entity.User;
import za.co.gymhubsa.api.common.exception.BadRequestException;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.entity.MemberStatus;
import za.co.gymhubsa.api.member.repository.MemberRepository;
import za.co.gymhubsa.api.notification.channel.EmailService;
import za.co.gymhubsa.api.notification.channel.PushNotificationService;
import za.co.gymhubsa.api.notification.channel.SmsService;
import za.co.gymhubsa.api.notification.channel.WhatsAppService;
import za.co.gymhubsa.api.notification.dto.NotificationResponse;
import za.co.gymhubsa.api.notification.dto.PromotionalBroadcastRequest;
import za.co.gymhubsa.api.notification.entity.NotificationChannel;
import za.co.gymhubsa.api.notification.entity.NotificationStatus;
import za.co.gymhubsa.api.notification.entity.NotificationType;
import za.co.gymhubsa.api.notification.repository.NotificationRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private SmsService smsService;
    @Mock
    private WhatsAppService whatsAppService;
    @Mock
    private PushNotificationService pushNotificationService;

    private NotificationService notificationService;

    private Member member;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, memberRepository,
                emailService, smsService, whatsAppService, pushNotificationService);

        User user = User.builder().firstName("Nthabiseng").lastName("Dlamini").email("nthabiseng@example.com")
                .phoneNumber("0821234567").password("hashed").role(Role.MEMBER).enabled(true).build();
        user.setId(1L);
        member = Member.builder().user(user).status(MemberStatus.ACTIVE).joinDate(LocalDate.now()).build();
        member.setId(1L);

        org.mockito.Mockito.lenient().when(notificationRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void sendToMember_viaEmail_usesUserEmailAndMarksSent() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        NotificationResponse response = notificationService.sendToMember(1L, NotificationChannel.EMAIL,
                NotificationType.GENERAL, "Hello", "Test message");

        assertThat(response.recipient()).isEqualTo("nthabiseng@example.com");
        assertThat(response.status()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void sendToMember_viaSmsWithNoPhoneNumber_throwsBadRequest() {
        member.getUser().setPhoneNumber(null);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> notificationService.sendToMember(1L, NotificationChannel.SMS,
                NotificationType.GENERAL, null, "Test message"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void sendToMember_whenChannelThrows_marksFailedButDoesNotThrow() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        doThrow(new RuntimeException("gateway down")).when(emailService).send(anyString(), anyString(), anyString());

        NotificationResponse response = notificationService.sendToMember(1L, NotificationChannel.EMAIL,
                NotificationType.GENERAL, "Hello", "Test message");

        assertThat(response.status()).isEqualTo(NotificationStatus.FAILED);
        assertThat(response.errorMessage()).isEqualTo("gateway down");
    }

    @Test
    void sendDirect_withNoMember_sendsAndPersistsWithoutMemberLink() {
        NotificationResponse response = notificationService.sendDirect(NotificationChannel.EMAIL,
                NotificationType.PASSWORD_RESET, "admin@example.com", "Reset", "token-123");

        assertThat(response.memberId()).isNull();
        assertThat(response.status()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void broadcastPromotional_withNoMemberIds_targetsAllActiveMembers() {
        when(memberRepository.findAll()).thenReturn(List.of(member));

        List<NotificationResponse> responses = notificationService.broadcastPromotional(
                new PromotionalBroadcastRequest(NotificationChannel.EMAIL, "Sale!", "20% off this week", null));

        assertThat(responses).hasSize(1);
    }

    @Test
    void broadcastPromotional_withSpecificMemberIds_targetsThoseOnly() {
        when(memberRepository.findAllById(List.of(1L))).thenReturn(List.of(member));

        List<NotificationResponse> responses = notificationService.broadcastPromotional(
                new PromotionalBroadcastRequest(NotificationChannel.EMAIL, "Sale!", "20% off this week", List.of(1L)));

        assertThat(responses).hasSize(1);
    }

    @Test
    void broadcastPromotional_withMemberMissingContactInfo_marksFailedButContinuesOthers() {
        member.getUser().setPhoneNumber(null);
        when(memberRepository.findAllById(List.of(1L))).thenReturn(List.of(member));

        List<NotificationResponse> responses = notificationService.broadcastPromotional(
                new PromotionalBroadcastRequest(NotificationChannel.SMS, null, "20% off this week", List.of(1L)));

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).status()).isEqualTo(NotificationStatus.FAILED);
    }
}
