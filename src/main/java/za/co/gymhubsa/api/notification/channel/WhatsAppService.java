package za.co.gymhubsa.api.notification.channel;

/** Abstraction over the WhatsApp Business API provider; swap in a real one later. */
public interface WhatsAppService {

    void send(String toPhoneNumber, String message);
}
