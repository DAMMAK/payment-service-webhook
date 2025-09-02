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
import dev.dammak.paymentservicewebhook.util.TestDataBuilder;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    private WebhookEndpointRepository endpointRepository;

    @Mock
    private EventSubscriptionRepository subscriptionRepository;

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private WebhookMapper mapper;

    @InjectMocks
    private WebhookService webhookService;

    private Merchant merchant;
    private WebhookEndpoint endpoint;
    private WebhookEndpointDTO endpointDTO;

    @BeforeEach
    void setUp() {
        merchant = TestDataBuilder.createMerchant();
        // Set ID for testing purposes
        merchant.setId(UUID.randomUUID());
        endpoint = TestDataBuilder.createWebhookEndpoint(merchant);
        // Set ID for testing purposes
        endpoint.setId(UUID.randomUUID());
        endpointDTO = TestDataBuilder.createWebhookEndpointDTO();
    }

    @Test
    void createEndpoint_shouldCreateEndpointWithSubscriptions() {
        // Given
        UUID merchantId = merchant.getId();
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
        when(mapper.toEndpointEntity(endpointDTO)).thenReturn(endpoint);
        when(endpointRepository.save(any(WebhookEndpoint.class))).thenReturn(endpoint);
        when(mapper.toEndpointDTO(endpoint)).thenReturn(endpointDTO);

        // When
        WebhookEndpointDTO result = webhookService.createEndpoint(merchantId, endpointDTO);

        // Then
        assertThat(result).isEqualTo(endpointDTO);

        // Verify repository interactions
        verify(merchantRepository).findById(merchantId);
        verify(endpointRepository).save(any(WebhookEndpoint.class));
        verify(subscriptionRepository, times(2)).save(any(EventSubscription.class)); // Two events in DTO

        // Verify that secret was generated
        ArgumentCaptor<WebhookEndpoint> endpointCaptor = ArgumentCaptor.forClass(WebhookEndpoint.class);
        verify(endpointRepository).save(endpointCaptor.capture());
        assertThat(endpointCaptor.getValue().getSecret()).startsWith("whsec_");
    }

    @Test
    void createEndpoint_shouldThrowExceptionWhenMerchantNotFound() {
        // Given
        UUID merchantId = UUID.randomUUID();
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> webhookService.createEndpoint(merchantId, endpointDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Merchant not found");

        // Verify repository interactions
        verify(merchantRepository).findById(merchantId);
        verifyNoInteractions(endpointRepository);
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    void updateEndpoint_shouldUpdateEndpointAndSubscriptions() {
        // Given
        UUID merchantId = merchant.getId();
        UUID endpointId = endpoint.getId();
        EventSubscription existingSubscription = TestDataBuilder.createEventSubscription(endpoint, "payment.created");
        existingSubscription.setIsActive(false); // Initially inactive
        
        // Set ID for testing purposes
        existingSubscription.setId(UUID.randomUUID());
        
        // Use ArrayList to make it mutable
        List<EventSubscription> subscriptions = new ArrayList<>();
        subscriptions.add(existingSubscription);
        endpoint.setSubscriptions(subscriptions);

        when(endpointRepository.findById(endpointId)).thenReturn(Optional.of(endpoint));
        when(endpointRepository.save(any(WebhookEndpoint.class))).thenReturn(endpoint);
        when(mapper.toEndpointDTO(endpoint)).thenReturn(endpointDTO);
        
        // Create a mock answer for the update method
        doAnswer(invocation -> {
            WebhookEndpointDTO dto = invocation.getArgument(0);
            WebhookEndpoint entity = invocation.getArgument(1);
            entity.setUrl(dto.getUrl());
            entity.setDescription(dto.getDescription());
            return null;
        }).when(mapper).updateEndpointFromDTO(endpointDTO, endpoint);

        // When
        WebhookEndpointDTO result = webhookService.updateEndpoint(merchantId, endpointId, endpointDTO);

        // Then
        assertThat(result).isEqualTo(endpointDTO);
        assertThat(existingSubscription.getIsActive()).isTrue(); // Should be reactivated

        // Verify repository interactions
        verify(endpointRepository).findById(endpointId);
        verify(endpointRepository).save(endpoint);
    }

    @Test
    void updateEndpoint_shouldThrowExceptionWhenEndpointNotFound() {
        // Given
        UUID merchantId = merchant.getId();
        UUID endpointId = UUID.randomUUID();
        when(endpointRepository.findById(endpointId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> webhookService.updateEndpoint(merchantId, endpointId, endpointDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Webhook endpoint not found");

        // Verify repository interactions
        verify(endpointRepository).findById(endpointId);
        verifyNoMoreInteractions(endpointRepository);
    }

    @Test
    void updateEndpoint_shouldThrowExceptionWhenMerchantDoesNotOwnEndpoint() {
        // Given
        UUID merchantId = UUID.randomUUID(); // Different merchant
        UUID endpointId = endpoint.getId();
        when(endpointRepository.findById(endpointId)).thenReturn(Optional.of(endpoint));

        // When & Then
        assertThatThrownBy(() -> webhookService.updateEndpoint(merchantId, endpointId, endpointDTO))
                .isInstanceOf(WebhookException.class)
                .hasMessage("Endpoint does not belong to this merchant");

        // Verify repository interactions
        verify(endpointRepository).findById(endpointId);
        verifyNoMoreInteractions(endpointRepository);
    }

    @Test
    void deleteEndpoint_shouldSetStatusToInactive() {
        // Given
        UUID merchantId = merchant.getId();
        UUID endpointId = endpoint.getId();
        when(endpointRepository.findById(endpointId)).thenReturn(Optional.of(endpoint));

        // When
        webhookService.deleteEndpoint(merchantId, endpointId);

        // Then
        assertThat(endpoint.getStatus()).isEqualTo(WebhookEndpoint.WebhookStatus.INACTIVE);
        verify(endpointRepository).findById(endpointId);
        verify(endpointRepository).save(endpoint);
    }

    @Test
    void deleteEndpoint_shouldThrowExceptionWhenEndpointNotFound() {
        // Given
        UUID merchantId = merchant.getId();
        UUID endpointId = UUID.randomUUID();
        when(endpointRepository.findById(endpointId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> webhookService.deleteEndpoint(merchantId, endpointId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Webhook endpoint not found");

        // Verify repository interactions
        verify(endpointRepository).findById(endpointId);
        verifyNoMoreInteractions(endpointRepository);
    }

    @Test
    void deleteEndpoint_shouldThrowExceptionWhenMerchantDoesNotOwnEndpoint() {
        // Given
        UUID merchantId = UUID.randomUUID(); // Different merchant
        UUID endpointId = endpoint.getId();
        when(endpointRepository.findById(endpointId)).thenReturn(Optional.of(endpoint));

        // When & Then
        assertThatThrownBy(() -> webhookService.deleteEndpoint(merchantId, endpointId))
                .isInstanceOf(WebhookException.class)
                .hasMessage("Endpoint does not belong to this merchant");

        // Verify repository interactions
        verify(endpointRepository).findById(endpointId);
        verifyNoMoreInteractions(endpointRepository);
    }

    @Test
    void getEndpoint_shouldReturnEndpointWithStatistics() {
        // Given
        UUID merchantId = merchant.getId();
        UUID endpointId = endpoint.getId();
        
        WebhookEndpoint endpointWithMerchant = TestDataBuilder.createWebhookEndpoint(merchant);
        endpointWithMerchant.setId(endpointId);
        // Set merchant ID to match
        when(endpointRepository.findByIdWithMerchant(endpointId)).thenReturn(Optional.of(endpointWithMerchant));
        when(mapper.toEndpointDTO(endpointWithMerchant)).thenReturn(endpointDTO);

        // When
        WebhookEndpointDTO result = webhookService.getEndpoint(merchantId, endpointId);

        // Then
        assertThat(result).isEqualTo(endpointDTO);
        verify(endpointRepository).findByIdWithMerchant(endpointId);
    }

    @Test
    void getEndpoint_shouldThrowExceptionWhenEndpointNotFound() {
        // Given
        UUID merchantId = merchant.getId();
        UUID endpointId = UUID.randomUUID();
        when(endpointRepository.findByIdWithMerchant(endpointId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> webhookService.getEndpoint(merchantId, endpointId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Webhook endpoint not found");

        // Verify repository interactions
        verify(endpointRepository).findByIdWithMerchant(endpointId);
    }

    @Test
    void getEndpoint_shouldThrowExceptionWhenMerchantDoesNotOwnEndpoint() {
        // Given
        UUID merchantId = UUID.randomUUID(); // Different merchant
        UUID endpointId = endpoint.getId();
        
        WebhookEndpoint endpointWithMerchant = TestDataBuilder.createWebhookEndpoint(merchant);
        endpointWithMerchant.setId(endpointId);
        when(endpointRepository.findByIdWithMerchant(endpointId)).thenReturn(Optional.of(endpointWithMerchant));

        // When & Then
        assertThatThrownBy(() -> webhookService.getEndpoint(merchantId, endpointId))
                .isInstanceOf(WebhookException.class)
                .hasMessage("Endpoint does not belong to this merchant");

        // Verify repository interactions
        verify(endpointRepository).findByIdWithMerchant(endpointId);
    }

    @Test
    void listEndpoints_shouldReturnPageOfEndpoints() {
        // Given
        UUID merchantId = merchant.getId();
        Pageable pageable = PageRequest.of(0, 10);
        Page<WebhookEndpoint> page = new PageImpl<>(List.of(endpoint));
        when(endpointRepository.findByMerchantId(merchantId, pageable)).thenReturn(page);
        when(mapper.toEndpointDTO(endpoint)).thenReturn(endpointDTO);

        // When
        Page<WebhookEndpointDTO> result = webhookService.listEndpoints(merchantId, null, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(endpointDTO);
        verify(endpointRepository).findByMerchantId(merchantId, pageable);
    }

    @Test
    void listEndpoints_shouldReturnPageOfEndpointsWithStatusFilter() {
        // Given
        UUID merchantId = merchant.getId();
        WebhookEndpoint.WebhookStatus status = WebhookEndpoint.WebhookStatus.ACTIVE;
        Pageable pageable = PageRequest.of(0, 10);
        Page<WebhookEndpoint> page = new PageImpl<>(List.of(endpoint));
        when(endpointRepository.findByMerchantIdAndStatus(merchantId, status, pageable)).thenReturn(page);
        when(mapper.toEndpointDTO(endpoint)).thenReturn(endpointDTO);

        // When
        Page<WebhookEndpointDTO> result = webhookService.listEndpoints(merchantId, status, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(endpointDTO);
        verify(endpointRepository).findByMerchantIdAndStatus(merchantId, status, pageable);
    }

    @Test
    void getActiveEndpointsForEvent_shouldReturnActiveEndpoints() {
        // Given
        String eventType = "payment.created";
        List<WebhookEndpoint> endpoints = List.of(endpoint);
        when(endpointRepository.findActiveEndpointsByEventType(eventType)).thenReturn(endpoints);

        // When
        List<WebhookEndpoint> result = webhookService.getActiveEndpointsForEvent(eventType);

        // Then
        assertThat(result).isEqualTo(endpoints);
        verify(endpointRepository).findActiveEndpointsByEventType(eventType);
    }
}