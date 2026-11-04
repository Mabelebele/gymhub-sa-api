package za.co.gymhubsa.api.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.gymhubsa.api.common.exception.BadRequestException;
import za.co.gymhubsa.api.common.exception.ResourceNotFoundException;
import za.co.gymhubsa.api.member.entity.Member;
import za.co.gymhubsa.api.member.entity.MemberStatus;
import za.co.gymhubsa.api.member.repository.MemberRepository;
import za.co.gymhubsa.api.notification.channel.EmailService;
import za.co.gymhubsa.api.notification.channel.PushNotificationService;
import za.co.gymhubsa.api.notification.channel.SmsService;
import za.co.gymhubsa.api.notification.channel.WhatsAppService;
import za.co.gymhubsa.api.notification.dto.NotificationResponse;
import za.co.gymhubsa.api.notification.dto.PromotionalBroadcastRequest;
import za.co.gymhubsa.api.notification.entity.Notification;
import za.co.gymhubsa.api.notification.entity.NotificationChannel;
import za.co.gymhubsa.api.notification.entity.NotificationStatus;
import za.co.gymhubsa.api.notification.entity.NotificationType;
import za.co.gymhubsa.api.notification.repository.NotificationRepository;

import java.time.Instant;
import java.util.List;

/**
 * Dispatches notifications through the appropriate channel (email/SMS/WhatsApp/push)
 * and persists a record of every attempt, regardless of outcome.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final WhatsAppService whatsAppService;
    private final PushNotificationService pushNotificationService;

    /** Sends a notification to a member, resolving their contact info for the given channel. */
    @Transactional
    public NotificationResponse sendToMember(Long memberId, NotificationChannel channel, NotificationType type,
                                              String subject, String message) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> ResourceNotFoundException.of("Member", memberId));

        String recipient = resolveRecipient(member, channel);
        return dispatch(member, recipient, channel, type, subject, message);
    }

    /** Sends a notification to an arbitrary recipient with no member link (e.g. admin/trainer password reset). */
    @Transactional
    public NotificationResponse sendDirect(NotificationChannel channel, NotificationType type, String recipient,
                                            String subject, String message) {
        return dispatch(null, recipient, channel, type, subject, message);
    }

    @Transactional
    public List<NotificationResponse> broadcastPromotional(PromotionalBroadcastRequest request) {
        List<Member> targets = (request.memberIds() == null || request.memberIds().isEmpty())
                ? memberRepository.findAll().stream().filter(m -> m.getStatus() == MemberStatus.ACTIVE).toList()
                : memberRepository.findAllById(request.memberIds());

        // Resolution failures (e.g. a member with no phone number for SMS) must not
        // abort the whole broadcast — record that recipient as FAILED and continue.
        return targets.stream()
                .map(member -> dispatchToMemberTolerant(member, request.channel(), NotificationType.PROMOTIONAL,
                        request.subject(), request.message()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getHistory(Long memberId, Pageable pageable) {
        return notificationRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable)
                .map(NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyHistory(String email, Pageable pageable) {
        Member member = memberRepository.findByUser_EmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member profile not found"));
        return getHistory(member.getId(), pageable);
    }

    private NotificationResponse dispatch(Member member, String recipient, NotificationChannel channel,
                                           NotificationType type, String subject, String message) {
        Notification notification = Notification.builder()
                .member(member)
                .recipient(recipient)
                .channel(channel)
                .type(type)
                .subject(subject)
                .message(message)
                .status(NotificationStatus.PENDING)
                .build();

        try {
            switch (channel) {
                case EMAIL -> emailService.send(recipient, subject, message);
                case SMS -> smsService.send(recipient, message);
                case WHATSAPP -> whatsAppService.send(recipient, message);
                case PUSH -> pushNotificationService.send(recipient, subject, message);
            }
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
        } catch (Exception ex) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(ex.getMessage());
        }

        notificationRepository.save(notification);
        return NotificationResponse.from(notification);
    }

    /**
     * Like {@link #dispatch}, but tolerates a recipient-resolution failure
     * (e.g. no phone number on file) by recording it as a FAILED notification
     * instead of throwing — used by bulk sends so one bad contact doesn't
     * abort the whole broadcast.
     */
    private NotificationResponse dispatchToMemberTolerant(Member member, NotificationChannel channel,
                                                           NotificationType type, String subject, String message) {
        try {
            String recipient = resolveRecipient(member, channel);
            return dispatch(member, recipient, channel, type, subject, message);
        } catch (BadRequestException ex) {
            Notification notification = Notification.builder()
                    .member(member)
                    .recipient(member.getUser().getEmail())
                    .channel(channel)
                    .type(type)
                    .subject(subject)
                    .message(message)
                    .status(NotificationStatus.FAILED)
                    .errorMessage(ex.getMessage())
                    .build();
            notificationRepository.save(notification);
            return NotificationResponse.from(notification);
        }
    }

    private String resolveRecipient(Member member, NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> member.getUser().getEmail();
            case SMS, WHATSAPP -> requireContact(member.getUser().getPhoneNumber(), "phone number");
            case PUSH -> "member:" + member.getId();
        };
    }

    private String requireContact(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Member has no " + label + " on file for this notification channel");
        }
        return value;
    }
}
