package za.co.gymhubsa.api.notification.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Dev/MVP stub: logs the email instead of sending it via a real provider. */
@Slf4j
@Service
public class LogEmailService implements EmailService {

    @Override
    public void send(String toEmail, String subject, String body) {
        log.info("[MOCK EMAIL] to={}, subject='{}', body='{}'", toEmail, subject, body);
    }
}
