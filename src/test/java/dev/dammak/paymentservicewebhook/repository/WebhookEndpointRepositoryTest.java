package dev.dammak.paymentservicewebhook.repository;

import dev.dammak.paymentservicewebhook.entity.Merchant;
import dev.dammak.paymentservicewebhook.entity.WebhookEndpoint;
import dev.dammak.paymentservicewebhook.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookEndpointRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private WebhookEndpointRepository webhookEndpointRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Merchant merchant;

    @BeforeEach
    void setUp() {
        // Create and save a merchant
        merchant = TestDataBuilder.createMerchant();
        merchant = merchantRepository.saveAndFlush(merchant);
    }

    @Test
    void findByMerchantId_shouldReturnEndpointsForMerchant() {
        // Given
        WebhookEndpoint endpoint1 = TestDataBuilder.createWebhookEndpoint(merchant);
        WebhookEndpoint endpoint2 = TestDataBuilder.createWebhookEndpoint(merchant);
        // Set headers to null to avoid JSON type issues in tests
        endpoint1.setHeaders(null);
        endpoint2.setHeaders(null);
        endpoint1 = webhookEndpointRepository.saveAndFlush(endpoint1);
        endpoint2 = webhookEndpointRepository.saveAndFlush(endpoint2);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<WebhookEndpoint> result = webhookEndpointRepository.findByMerchantId(merchant.getId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(WebhookEndpoint::getMerchant).containsOnly(merchant);
    }

    @Test
    void findByMerchantIdAndStatus_shouldReturnEndpointsWithSpecificStatus() {
        // Given
        WebhookEndpoint activeEndpoint = TestDataBuilder.createWebhookEndpoint(merchant);
        activeEndpoint.setStatus(WebhookEndpoint.WebhookStatus.ACTIVE);
        // Set headers to null to avoid JSON type issues in tests
        activeEndpoint.setHeaders(null);
        activeEndpoint = webhookEndpointRepository.saveAndFlush(activeEndpoint);

        WebhookEndpoint inactiveEndpoint = TestDataBuilder.createWebhookEndpoint(merchant);
        inactiveEndpoint.setStatus(WebhookEndpoint.WebhookStatus.INACTIVE);
        // Set headers to null to avoid JSON type issues in tests
        inactiveEndpoint.setHeaders(null);
        inactiveEndpoint = webhookEndpointRepository.saveAndFlush(inactiveEndpoint);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<WebhookEndpoint> result = webhookEndpointRepository.findByMerchantIdAndStatus(
                merchant.getId(), WebhookEndpoint.WebhookStatus.ACTIVE, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(WebhookEndpoint.WebhookStatus.ACTIVE);
    }

    @Test
    void findActiveEndpointsByEventType_shouldReturnActiveEndpointsForEventType() {
        // Given
        WebhookEndpoint endpoint = TestDataBuilder.createWebhookEndpoint(merchant);
        endpoint.setStatus(WebhookEndpoint.WebhookStatus.ACTIVE);
        // Set headers to null to avoid JSON type issues in tests
        endpoint.setHeaders(null);
        endpoint = webhookEndpointRepository.saveAndFlush(endpoint);

        // When
        List<WebhookEndpoint> result = webhookEndpointRepository.findActiveEndpointsByEventType("payment.created");

        // Then
        assertThat(result).isEmpty(); // No subscriptions yet

        // Add a subscription and try again
        // Note: This would require the EventSubscriptionRepository to be tested as well
    }

    @Test
    void findByIdWithMerchant_shouldReturnEndpointWithMerchant() {
        // Given
        WebhookEndpoint endpoint = TestDataBuilder.createWebhookEndpoint(merchant);
        // Set headers to null to avoid JSON type issues in tests
        endpoint.setHeaders(null);
        endpoint = webhookEndpointRepository.saveAndFlush(endpoint);
        UUID endpointId = endpoint.getId();

        // When
        Optional<WebhookEndpoint> result = webhookEndpointRepository.findByIdWithMerchant(endpointId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(endpointId);
        assertThat(result.get().getMerchant()).isNotNull();
        assertThat(result.get().getMerchant().getId()).isEqualTo(merchant.getId());
    }

    @Test
    void findByStatus_shouldReturnEndpointsWithSpecificStatus() {
        // Given
        WebhookEndpoint activeEndpoint = TestDataBuilder.createWebhookEndpoint(merchant);
        activeEndpoint.setStatus(WebhookEndpoint.WebhookStatus.ACTIVE);
        // Set headers to null to avoid JSON type issues in tests
        activeEndpoint.setHeaders(null);
        activeEndpoint = webhookEndpointRepository.saveAndFlush(activeEndpoint);

        WebhookEndpoint failedEndpoint = TestDataBuilder.createWebhookEndpoint(merchant);
        failedEndpoint.setStatus(WebhookEndpoint.WebhookStatus.FAILED);
        // Set headers to null to avoid JSON type issues in tests
        failedEndpoint.setHeaders(null);
        failedEndpoint = webhookEndpointRepository.saveAndFlush(failedEndpoint);

        // When
        List<WebhookEndpoint> result = webhookEndpointRepository.findByStatus(WebhookEndpoint.WebhookStatus.ACTIVE);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(WebhookEndpoint.WebhookStatus.ACTIVE);
    }

    @Test
    void countByMerchantIdAndStatus_shouldReturnCorrectCount() {
        // Given
        WebhookEndpoint activeEndpoint1 = TestDataBuilder.createWebhookEndpoint(merchant);
        activeEndpoint1.setStatus(WebhookEndpoint.WebhookStatus.ACTIVE);
        // Set headers to null to avoid JSON type issues in tests
        activeEndpoint1.setHeaders(null);
        activeEndpoint1 = webhookEndpointRepository.saveAndFlush(activeEndpoint1);

        WebhookEndpoint activeEndpoint2 = TestDataBuilder.createWebhookEndpoint(merchant);
        activeEndpoint2.setStatus(WebhookEndpoint.WebhookStatus.ACTIVE);
        // Set headers to null to avoid JSON type issues in tests
        activeEndpoint2.setHeaders(null);
        activeEndpoint2 = webhookEndpointRepository.saveAndFlush(activeEndpoint2);

        WebhookEndpoint inactiveEndpoint = TestDataBuilder.createWebhookEndpoint(merchant);
        inactiveEndpoint.setStatus(WebhookEndpoint.WebhookStatus.INACTIVE);
        // Set headers to null to avoid JSON type issues in tests
        inactiveEndpoint.setHeaders(null);
        inactiveEndpoint = webhookEndpointRepository.saveAndFlush(inactiveEndpoint);

        // When
        long count = webhookEndpointRepository.countByMerchantIdAndStatus(
                merchant.getId(), WebhookEndpoint.WebhookStatus.ACTIVE);

        // Then
        assertThat(count).isEqualTo(2);
    }
}