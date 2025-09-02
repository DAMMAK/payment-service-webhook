package dev.dammak.paymentservicewebhook.util;

import dev.dammak.paymentservicewebhook.dto.WebhookEndpointDTO;
import dev.dammak.paymentservicewebhook.entity.EventSubscription;
import dev.dammak.paymentservicewebhook.entity.Merchant;
import dev.dammak.paymentservicewebhook.entity.WebhookEndpoint;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestDataBuilder {

    public static Merchant createMerchant() {
        Merchant merchant = new Merchant();
        // Don't manually set ID - let Hibernate generate it
        merchant.setName("Test Merchant");
        merchant.setApiKey("test-api-key");
        merchant.setSecretKey("test-secret-key");
        merchant.setEmail("test@example.com");
        merchant.setWebhookSecret("test-webhook-secret");
        // Don't manually set version - let Hibernate manage it
        return merchant;
    }

    public static WebhookEndpoint createWebhookEndpoint(Merchant merchant) {
        WebhookEndpoint endpoint = new WebhookEndpoint();
        // Don't manually set ID - let Hibernate generate it
        endpoint.setMerchant(merchant);
        endpoint.setUrl("https://example.com/webhook");
        endpoint.setDescription("Test webhook endpoint");
        endpoint.setSecret("test-secret");
        endpoint.setRetryEnabled(true);
        endpoint.setMaxRetries(3);
        endpoint.setTimeoutSeconds(30);
        endpoint.setHeaders(Map.of("X-Custom-Header", "test-value"));
        endpoint.setSubscriptions(new ArrayList<>());
        // Don't manually set version - let Hibernate manage it
        return endpoint;
    }

    public static EventSubscription createEventSubscription(WebhookEndpoint endpoint, String eventType) {
        EventSubscription subscription = new EventSubscription();
        // Don't manually set ID - let Hibernate generate it
        subscription.setWebhookEndpoint(endpoint);
        subscription.setEventType(eventType);
        subscription.setIsActive(true);
        // Don't manually set version - let Hibernate manage it
        return subscription;
    }

    public static WebhookEndpointDTO createWebhookEndpointDTO() {
        WebhookEndpointDTO dto = new WebhookEndpointDTO();
        dto.setUrl("https://example.com/webhook");
        dto.setDescription("Test webhook endpoint");
        dto.setRetryEnabled(true);
        dto.setMaxRetries(3);
        dto.setTimeoutSeconds(30);
        dto.setHeaders(Map.of("X-Custom-Header", "test-value"));
        dto.setEvents(List.of("payment.created", "payment.updated"));
        return dto;
    }
}