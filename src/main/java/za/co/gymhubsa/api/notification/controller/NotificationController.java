package za.co.gymhubsa.api.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.co.gymhubsa.api.common.dto.ApiResponse;
import za.co.gymhubsa.api.notification.dto.NotificationResponse;
import za.co.gymhubsa.api.notification.dto.PromotionalBroadcastRequest;
import za.co.gymhubsa.api.notification.dto.SendNotificationRequest;
import za.co.gymhubsa.api.notification.service.NotificationService;

import java.util.List;

@Tag(name = "Notifications", description = "Send and track SMS/WhatsApp/email/push notifications")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Send a notification to a member (membership expiry, payment, appointment reminder, or general)")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<NotificationResponse>> send(@Valid @RequestBody SendNotificationRequest request) {
        NotificationResponse response = notificationService.sendToMember(request.memberId(), request.channel(),
                request.type(), request.subject(), request.message());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Broadcast a promotional notification to specific members, or all active members if none specified")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/promotional/broadcast")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> broadcastPromotional(
            @Valid @RequestBody PromotionalBroadcastRequest request) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.broadcastPromotional(request)));
    }

    @Operation(summary = "Get a member's notification history, paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/member/{memberId}/history")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getHistory(
            @PathVariable Long memberId, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getHistory(memberId, pageable)));
    }

    @Operation(summary = "Get the signed-in member's own notification history")
    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me/history")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getMyHistory(
            Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getMyHistory(authentication.getName(), pageable)));
    }
}
