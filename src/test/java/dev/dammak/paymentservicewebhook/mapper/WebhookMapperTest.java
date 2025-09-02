package dev.dammak.paymentservicewebhook.mapper;

import dev.dammak.paymentservicewebhook.dto.WebhookDeliveryDTO;
import dev.dammak.paymentservicewebhook.dto.WebhookEndpointDTO;
import dev.dammak.paymentservicewebhook.dto.WebhookEventDTO;
import dev.dammak.paymentservicewebhook.entity.Merchant;
import dev.dammak.paymentservicewebhook.entity.WebhookDelivery;
import dev.dammak.paymentservicewebhook.entity.WebhookEndpoint;
import dev.dammak.paymentservicewebhook.entity.WebhookEvent;
import dev.dammak.paymentservicewebhook.util.TestDataBuilder;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookMapperTest {

    private final WebhookMapper webhookMapper = Mappers.getMapper(WebhookMapper.class);

    @Test
    void toEndpointDTO_shouldMapEntityToDTO() {
        // Given
        Merchant merchant = TestDataBuilder.createMerchant();
        merchant.setId(UUID.randomUUID());
        
        WebhookEndpoint endpoint = TestDataBuilder.createWebhookEndpoint(merchant);
        endpoint.setId(UUID.randomUUID());
        endpoint.setHeaders(Map.of("X-Custom-Header", "test-value"));
        
        // When
        WebhookEndpointDTO dto = webhookMapper.toEndpointDTO(endpoint);
        
        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(endpoint.getId());
        assertThat(dto.getUrl()).isEqualTo(endpoint.getUrl());
        assertThat(dto.getDescription()).isEqualTo(endpoint.getDescription());
        assertThat(dto.getRetryEnabled()).isEqualTo(endpoint.getRetryEnabled());
        assertThat(dto.getMaxRetries()).isEqualTo(endpoint.getMaxRetries());
        assertThat(dto.getTimeoutSeconds()).isEqualTo(endpoint.getTimeoutSeconds());
        assertThat(dto.getHeaders()).containsEntry("X-Custom-Header", "test-value");
    }

    @Test
    void toEndpointEntity_shouldMapDTOToEntity() {
        // Given
        WebhookEndpointDTO dto = TestDataBuilder.createWebhookEndpointDTO();
        dto.setHeaders(Map.of("X-Custom-Header", "test-value"));
        
        // When
        WebhookEndpoint entity = webhookMapper.toEndpointEntity(dto);
        
        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getUrl()).isEqualTo(dto.getUrl());
        assertThat(entity.getDescription()).isEqualTo(dto.getDescription());
        assertThat(entity.getRetryEnabled()).isEqualTo(dto.getRetryEnabled());
        assertThat(entity.getMaxRetries()).isEqualTo(dto.getMaxRetries());
        assertThat(entity.getTimeoutSeconds()).isEqualTo(dto.getTimeoutSeconds());
        assertThat(entity.getHeaders()).containsEntry("X-Custom-Header", "test-value");
    }

    @Test
    void toEndpointDTOList_shouldMapEntityListToDTOList() {
        // Given
        Merchant merchant = TestDataBuilder.createMerchant();
        merchant.setId(UUID.randomUUID());
        
        WebhookEndpoint endpoint1 = TestDataBuilder.createWebhookEndpoint(merchant);
        endpoint1.setId(UUID.randomUUID());
        WebhookEndpoint endpoint2 = TestDataBuilder.createWebhookEndpoint(merchant);
        endpoint2.setId(UUID.randomUUID());
        
        List<WebhookEndpoint> endpoints = Arrays.asList(endpoint1, endpoint2);
        
        // When
        List<WebhookEndpointDTO> dtos = webhookMapper.toEndpointDTOList(endpoints);
        
        // Then
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getId()).isEqualTo(endpoint1.getId());
        assertThat(dtos.get(1).getId()).isEqualTo(endpoint2.getId());
    }

    @Test
    void toEventDTO_shouldMapEntityToDTO() {
        // Given
        WebhookEvent event = new WebhookEvent();
        event.setId(UUID.randomUUID());
        event.setEventType("payment.created");
        event.setProvider("stripe");
        event.setPayload(Map.of("amount", 1000));
        event.setIdempotencyKey("key-123");
        event.setStatus(WebhookEvent.EventStatus.PENDING);
        event.setSourceIp("192.168.1.1");
        event.setMetadata(Map.of("source", "api"));
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        
        // When
        WebhookEventDTO dto = webhookMapper.toEventDTO(event);
        
        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(event.getId());
        assertThat(dto.getEventType()).isEqualTo(event.getEventType());
        assertThat(dto.getProvider()).isEqualTo(event.getProvider());
        assertThat(dto.getPayload()).containsEntry("amount", 1000);
        assertThat(dto.getIdempotencyKey()).isEqualTo(event.getIdempotencyKey());
        assertThat(dto.getStatus()).isEqualTo(event.getStatus().name());
        assertThat(dto.getMetadata()).containsEntry("source", "api");
    }
}