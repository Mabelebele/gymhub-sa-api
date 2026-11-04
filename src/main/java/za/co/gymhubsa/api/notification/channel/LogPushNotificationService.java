package za.co.gymhubsa.api.notification.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Dev/MVP stub: logs the push notification instead of sending it via FCM/APNs. */
@Slf4j
@Service
public class LogPushNotificationService implements PushNotificationService {

    @Override
    public void send(String recipient, String title, String body) {
        log.info("[MOCK PUSH] to={}, title='{}', body='{}'", recipient, title, body);
    }
}
