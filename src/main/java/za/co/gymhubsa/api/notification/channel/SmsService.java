package za.co.gymhubsa.api.notification.channel;

/** Abstraction over the SMS gateway; swap in a real one (e.g. Twilio, Clickatell) later. */
public interface SmsService {

    void send(String toPhoneNumber, String message);
}
