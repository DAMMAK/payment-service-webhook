package dev.dammak.paymentservicewebhook.service;
import dev.dammak.paymentservicewebhook.dto.WebhookEndpointDTO;
import dev.dammak.paymentservicewebhook.entity.EventSubscription;
import dev.dammak.paymentservicewebhook.entity.Merchant;
import dev.dammak.paymentservicewebhook.entity.WebhookEndpoint;
import dev.dammak.paymentservicewebhook.exception.WebhookException;
import dev.dammak.paymentservicewebhook.mapper.WebhookMapper;
import dev.dammak.paymentservicewebhook.repository.EventSubscriptionRepository;
import dev.dammak.paymentservicewebhook.repository.MerchantRepository;
import dev.dammak.paymentservicewebhook.repository.WebhookEndpointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
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
public class WebhookService {

    private final WebhookEndpointRepository endpointRepository;
    private final EventSubscriptionRepository subscriptionRepository;
    private final MerchantRepository merchantRepository;
    private final WebhookMapper mapper;
    private final SecureRandom secureRandom = new SecureRandom();

    public WebhookEndpointDTO createEndpoint(UUID merchantId, WebhookEndpointDTO dto) {
        log.info("Creating webhook endpoint for merchant: {}", merchantId);

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

        WebhookEndpoint endpoint = mapper.toEndpointEntity(dto);
        endpoint.setMerchant(merchant);
        endpoint.setSecret(generateSecret());

        endpoint = endpointRepository.save(endpoint);

        // Create event subscriptions
        if (dto.getEvents() != null) {
            for (String eventType : dto.getEvents()) {
                EventSubscription subscription = EventSubscription.builder()
                        .webhookEndpoint(endpoint)
                        .eventType(eventType)
                        .isActive(true)
                        .build();
                subscriptionRepository.save(subscription);
            }
        }

        return mapper.toEndpointDTO(endpoint);
    }

    public WebhookEndpointDTO updateEndpoint(UUID merchantId, UUID endpointId, WebhookEndpointDTO dto) {
        log.info("Updating webhook endpoint: {} for merchant: {}", endpointId, merchantId);

        WebhookEndpoint endpoint = endpointRepository.findById(endpointId)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook endpoint not found"));

        if (!endpoint.getMerchant().getId().equals(merchantId)) {
            throw new WebhookException("Endpoint does not belong to this merchant");
        }

        mapper.updateEndpointFromDTO(dto, endpoint);

        // Update event subscriptions
        if (dto.getEvents() != null) {
            // Deactivate existing subscriptions
            endpoint.getSubscriptions().forEach(sub -> sub.setIsActive(false));

            // Add or reactivate subscriptions
            for (String eventType : dto.getEvents()) {
                EventSubscription existing = endpoint.getSubscriptions().stream()
                        .filter(sub -> sub.getEventType().equals(eventType))
                        .findFirst()
                        .orElse(null);

                if (existing != null) {
                    existing.setIsActive(true);
                } else {
                    EventSubscription subscription = EventSubscription.builder()
                            .webhookEndpoint(endpoint)
                            .eventType(eventType)
                            .isActive(true)
                            .build();
                    endpoint.getSubscriptions().add(subscription);
                }
            }
        }

        endpoint = endpointRepository.save(endpoint);
        return mapper.toEndpointDTO(endpoint);
    }

    public void deleteEndpoint(UUID merchantId, UUID endpointId) {
        log.info("Deleting webhook endpoint: {} for merchant: {}", endpointId, merchantId);

        WebhookEndpoint endpoint = endpointRepository.findById(endpointId)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook endpoint not found"));

        if (!endpoint.getMerchant().getId().equals(merchantId)) {
            throw new WebhookException("Endpoint does not belong to this merchant");
        }

        endpoint.setStatus(WebhookEndpoint.WebhookStatus.INACTIVE);
        endpointRepository.save(endpoint);
    }

    public WebhookEndpointDTO getEndpoint(UUID merchantId, UUID endpointId) {
        WebhookEndpoint endpoint = endpointRepository.findByIdWithMerchant(endpointId)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook endpoint not found"));

        if (!endpoint.getMerchant().getId().equals(merchantId)) {
            throw new WebhookException("Endpoint does not belong to this merchant");
        }

        WebhookEndpointDTO dto = mapper.toEndpointDTO(endpoint);

        // Add statistics
        dto.setStatistics(calculateStatistics(endpointId));

        return dto;
    }

    public Page<WebhookEndpointDTO> listEndpoints(UUID merchantId,
                                                  WebhookEndpoint.WebhookStatus status,
                                                  Pageable pageable) {
        Page<WebhookEndpoint> endpoints;

        if (status != null) {
            endpoints = endpointRepository.findByMerchantIdAndStatus(merchantId, status, pageable);
        } else {
            endpoints = endpointRepository.findByMerchantId(merchantId, pageable);
        }

        return endpoints.map(mapper::toEndpointDTO);
    }

    public List<WebhookEndpoint> getActiveEndpointsForEvent(String eventType) {
        return endpointRepository.findActiveEndpointsByEventType(eventType);
    }

    private String generateSecret() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return "whsec_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private WebhookEndpointDTO.WebhookStatisticsDTO calculateStatistics(UUID endpointId) {
        // This would typically query delivery statistics from the database
        return WebhookEndpointDTO.WebhookStatisticsDTO.builder()
                .totalDeliveries(0L)
                .successfulDeliveries(0L)
                .failedDeliveries(0L)
                .successRate(0.0)
                .averageLatencyMs(0.0)
                .build();
    }
}
