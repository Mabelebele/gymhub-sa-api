package za.co.gymhubsa.api.notification.channel;

/** Abstraction over the email provider; swap in a real one (e.g. SendGrid) later. */
public interface EmailService {

    void send(String toEmail, String subject, String body);
}
