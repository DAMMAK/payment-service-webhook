package dev.dammak.paymentservicewebhook.service;

import dev.dammak.paymentservicewebhook.dto.WebhookEventDTO;
import dev.dammak.paymentservicewebhook.entity.WebhookEndpoint;
import dev.dammak.paymentservicewebhook.entity.WebhookEvent;
import dev.dammak.paymentservicewebhook.exception.DuplicateEventException;
import dev.dammak.paymentservicewebhook.mapper.WebhookMapper;
import dev.dammak.paymentservicewebhook.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
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
public class EventProcessingService {

    private final WebhookEventRepository eventRepository;
    private final WebhookService webhookService;
    private final DeliveryService deliveryService;
    private final WebhookMapper mapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public WebhookEventDTO processIncomingEvent(String provider,
                                                String eventType,
                                                Map<String, Object> payload,
                                                String idempotencyKey,
                                                String sourceIp) {
        log.info("Processing incoming event: {} from provider: {}", eventType, provider);

        // Check for duplicate event
        if (idempotencyKey != null) {
            eventRepository.findByIdempotencyKey(idempotencyKey)
                    .ifPresent(existing -> {
                        log.warn("Duplicate event with idempotency key: {}", idempotencyKey);
                        throw new DuplicateEventException("Event already processed");
                    });
        }

        // Create and save event
        WebhookEvent event = WebhookEvent.builder()
                .provider(provider)
                .eventType(eventType)
                .payload(payload)
                .idempotencyKey(idempotencyKey)
                .sourceIp(sourceIp)
                .status(WebhookEvent.EventStatus.PENDING)
                .build();

        event = eventRepository.save(event);

        // Find active endpoints subscribed to this event
        List<WebhookEndpoint> endpoints = webhookService.getActiveEndpointsForEvent(eventType);

        if (endpoints.isEmpty()) {
            log.warn("No active endpoints found for event type: {}", eventType);
            event.setStatus(WebhookEvent.EventStatus.DELIVERED);
            eventRepository.save(event);
        } else {
            // Queue deliveries
            for (WebhookEndpoint endpoint : endpoints) {
                queueDelivery(event.getId(), endpoint.getId());
            }
            event.setStatus(WebhookEvent.EventStatus.PROCESSING);
            eventRepository.save(event);
        }

        return mapper.toEventDTO(event);
    }

    private void queueDelivery(UUID eventId, UUID endpointId) {
        Map<String, Object> deliveryMessage = Map.of(
                "eventId", eventId.toString(),
                "endpointId", endpointId.toString(),
                "timestamp", System.currentTimeMillis()
        );

        kafkaTemplate.send("webhook-deliveries", deliveryMessage);
        log.debug("Queued delivery for event: {} to endpoint: {}", eventId, endpointId);
    }

    public void processStaleEvents() {
        // This method would be called by a scheduled job to handle stale events
        log.info("Processing stale events");
        // Implementation would mark old pending events as expired
    }
}
