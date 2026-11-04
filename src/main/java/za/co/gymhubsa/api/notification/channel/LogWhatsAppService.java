package za.co.gymhubsa.api.notification.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Dev/MVP stub: logs the WhatsApp message instead of sending it via a real provider. */
@Slf4j
@Service
public class LogWhatsAppService implements WhatsAppService {

    @Override
    public void send(String toPhoneNumber, String message) {
        log.info("[MOCK WHATSAPP] to={}, message='{}'", toPhoneNumber, message);
    }
}
