package dev.dammak.paymentservicewebhook.service;

import dev.dammak.paymentservicewebhook.entity.WebhookDelivery;
import dev.dammak.paymentservicewebhook.entity.WebhookEndpoint;
import dev.dammak.paymentservicewebhook.entity.WebhookEvent;
import dev.dammak.paymentservicewebhook.exception.WebhookDeliveryException;
import dev.dammak.paymentservicewebhook.repository.WebhookDeliveryRepository;
import dev.dammak.paymentservicewebhook.repository.WebhookEndpointRepository;
import dev.dammak.paymentservicewebhook.repository.WebhookEventRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
/**
 * Created By damola.adekoya on 01/09/2025
 *
 * @Author: damola.adekoya
 * @Email: adekoyafelix@gmail.com
 * @Date: 01/09/2025
 * @Project: payment-service-webhook
 */

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DeliveryService {

    private final WebhookDeliveryRepository deliveryRepository;
    private final WebhookEventRepository eventRepository;
    private final WebhookEndpointRepository endpointRepository;
    private final SignatureService signatureService;
    private final RestTemplate restTemplate;

    @Value("${webhook.delivery.timeout:30000}")
    private int deliveryTimeout;

    @Async("webhookExecutor")
    @CircuitBreaker(name = "webhookDelivery", fallbackMethod = "deliveryFallback")
    @Retry(name = "webhookDelivery")
    public CompletableFuture<WebhookDelivery> deliverWebhook(UUID eventId, UUID endpointId) {
        log.info("Delivering webhook event: {} to endpoint: {}", eventId, endpointId);

        long startTime = System.currentTimeMillis();

        WebhookEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new WebhookDeliveryException("Event not found"));

        WebhookEndpoint endpoint = endpointRepository.findById(endpointId)
                .orElseThrow(() -> new WebhookDeliveryException("Endpoint not found"));

        // Check for existing delivery attempts
        int attemptNumber = getNextAttemptNumber(eventId, endpointId);

        // Create delivery record
        WebhookDelivery delivery = WebhookDelivery.builder()
                .webhookEvent(event)
                .webhookEndpoint(endpoint)
                .status(WebhookDelivery.DeliveryStatus.IN_PROGRESS)
                .attemptNumber(attemptNumber)
                .build();

        delivery = deliveryRepository.save(delivery);

        try {
            // Prepare headers
            HttpHeaders headers = createHeaders(event, endpoint);

            // Create request
            HttpEntity<Object> request = new HttpEntity<>(event.getPayload(), headers);

            // Send webhook
            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint.getUrl(),
                    HttpMethod.POST,
                    request,
                    String.class
            );

            long latency = System.currentTimeMillis() - startTime;

            // Update delivery record with success
            delivery.setStatus(WebhookDelivery.DeliveryStatus.SUCCESS);
            delivery.setResponseStatus(response.getStatusCodeValue());
            delivery.setResponseBody(truncateResponseBody(response.getBody()));
            delivery.setDeliveredAt(LocalDateTime.now());
            delivery.setLatencyMs(latency);

            log.info("Successfully delivered webhook to {} in {}ms", endpoint.getUrl(), latency);

        } catch (Exception e) {
            log.error("Failed to deliver webhook to {}: {}", endpoint.getUrl(), e.getMessage());

            long latency = System.currentTimeMillis() - startTime;

            // Update delivery record with failure
            delivery.setStatus(WebhookDelivery.DeliveryStatus.FAILED);
            delivery.setErrorMessage(e.getMessage());
            delivery.setLatencyMs(latency);

            // Schedule retry if enabled
            if (endpoint.getRetryEnabled() && attemptNumber < endpoint.getMaxRetries()) {
                scheduleRetry(delivery);
            } else {
                delivery.setStatus(WebhookDelivery.DeliveryStatus.MAX_RETRIES_EXCEEDED);
            }
        }

        delivery = deliveryRepository.save(delivery);

        // Update event status if all deliveries are complete
        updateEventStatus(eventId);

        return CompletableFuture.completedFuture(delivery);
    }

    private HttpHeaders createHeaders(WebhookEvent event, WebhookEndpoint endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Add signature
        String signature = signatureService.generateSignature(
                event.getPayload(),
                endpoint.getSecret()
        );
        headers.set("X-Webhook-Signature", signature);

        // Add event metadata
        headers.set("X-Webhook-Event-Id", event.getId().toString());
        headers.set("X-Webhook-Event-Type", event.getEventType());
        headers.set("X-Webhook-Timestamp", String.valueOf(System.currentTimeMillis()));

        // Add custom headers if configured
        if (endpoint.getHeaders() != null) {
            endpoint.getHeaders().forEach((key, value) ->
                    headers.set(key, value.toString()));
        }

        return headers;
    }

    private void scheduleRetry(WebhookDelivery delivery) {
        int attemptNumber = delivery.getAttemptNumber();
        long delaySeconds = calculateRetryDelay(attemptNumber);

        delivery.setStatus(WebhookDelivery.DeliveryStatus.RETRY_SCHEDULED);
        delivery.setNextRetryAt(LocalDateTime.now().plusSeconds(delaySeconds));

        log.info("Scheduled retry for delivery {} in {} seconds",
                delivery.getId(), delaySeconds);
    }

    private long calculateRetryDelay(int attemptNumber) {
        // Exponential backoff: 2^attempt * base_delay
        long baseDelay = 10; // 10 seconds
        return Math.min((long) Math.pow(2, attemptNumber) * baseDelay, 3600); // Max 1 hour
    }

    private int getNextAttemptNumber(UUID eventId, UUID endpointId) {
        return deliveryRepository.findByEventAndEndpoint(eventId, endpointId)
                .stream()
                .mapToInt(WebhookDelivery::getAttemptNumber)
                .max()
                .orElse(0) + 1;
    }

    private String truncateResponseBody(String responseBody) {
        if (responseBody == null) return null;
        int maxLength = 1000;
        return responseBody.length() > maxLength
                ? responseBody.substring(0, maxLength) + "..."
                : responseBody;
    }

    private void updateEventStatus(UUID eventId) {
        // Check if all deliveries are complete for this event
        // Update event status accordingly
    }

    public CompletableFuture<WebhookDelivery> deliveryFallback(UUID eventId,
                                                               UUID endpointId,
                                                               Exception ex) {
        log.error("Circuit breaker activated for delivery to endpoint: {}", endpointId, ex);

        WebhookDelivery delivery = WebhookDelivery.builder()
                .status(WebhookDelivery.DeliveryStatus.FAILED)
                .errorMessage("Circuit breaker activated: " + ex.getMessage())
                .build();

        return CompletableFuture.completedFuture(delivery);
    }
}