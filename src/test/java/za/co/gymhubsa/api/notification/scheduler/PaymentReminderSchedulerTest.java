package za.co.gymhubsa.api.notification.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.gymhubsa.api.notification.entity.NotificationChannel;
import za.co.gymhubsa.api.notification.entity.NotificationType;
import za.co.gymhubsa.api.notification.service.NotificationService;
import za.co.gymhubsa.api.payment.dto.OutstandingBalanceResponse;
import za.co.gymhubsa.api.payment.service.PaymentService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentReminderSchedulerTest {

    @Mock
    private PaymentService paymentService;
    @Mock
    private NotificationService notificationService;

    @Test
    void sendPaymentReminders_notifiesEachMemberWithOutstandingBalance() {
        PaymentReminderScheduler scheduler = new PaymentReminderScheduler(paymentService, notificationService);

        OutstandingBalanceResponse balance = new OutstandingBalanceResponse(10L, "Jane Doe",
                new BigDecimal("499.00"), new BigDecimal("200.00"), new BigDecimal("299.00"));
        when(paymentService.getOutstandingBalances()).thenReturn(List.of(balance));

        scheduler.sendPaymentReminders();

        verify(notificationService).sendToMember(eq(10L), eq(NotificationChannel.EMAIL),
                eq(NotificationType.PAYMENT_REMINDER), any(), any());
    }
}
