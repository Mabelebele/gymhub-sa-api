package za.co.gymhubsa.api.notification.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Dev/MVP stub: logs the SMS instead of sending it via a real gateway. */
@Slf4j
@Service
public class LogSmsService implements SmsService {

    @Override
    public void send(String toPhoneNumber, String message) {
        log.info("[MOCK SMS] to={}, message='{}'", toPhoneNumber, message);
    }
}
