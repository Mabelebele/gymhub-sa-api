package za.co.gymhubsa.api.payment.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.payment-gateway")
public record PaymentGatewayProperties(String webhookSecret) {
}
