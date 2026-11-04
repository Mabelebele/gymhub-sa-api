package za.co.gymhubsa.api.notification.channel;

/**
 * Abstraction over mobile push notifications; swap in a real provider (e.g.
 * Firebase Cloud Messaging) later. {@code recipient} is a placeholder member
 * identifier for now, since device-token registration isn't modeled yet.
 */
public interface PushNotificationService {

    void send(String recipient, String title, String body);
}
