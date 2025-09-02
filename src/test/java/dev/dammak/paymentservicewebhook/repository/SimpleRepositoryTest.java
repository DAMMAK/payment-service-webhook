package dev.dammak.paymentservicewebhook.repository;

import dev.dammak.paymentservicewebhook.entity.Merchant;
import dev.dammak.paymentservicewebhook.entity.WebhookEndpoint;
import dev.dammak.paymentservicewebhook.repository.MerchantRepository;
import dev.dammak.paymentservicewebhook.repository.WebhookEndpointRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(RepositoryTestConfig.class)
class SimpleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private WebhookEndpointRepository webhookEndpointRepository;

    @Test
    void testMerchantEntityMapping() {
        // Given
        Merchant merchant = Merchant.builder()
                .name("Test Merchant")
                .apiKey("test-api-key")
                .secretKey("test-secret-key")
                .email("test@example.com")
                .build();

        // When
        Merchant savedMerchant = entityManager.persistAndFlush(merchant);
        entityManager.clear();

        // Then
        Merchant foundMerchant = merchantRepository.findById(savedMerchant.getId()).orElse(null);
        assertThat(foundMerchant).isNotNull();
        assertThat(foundMerchant.getName()).isEqualTo("Test Merchant");
    }

    @Test
    void testWebhookEndpointEntityMapping() {
        // Given
        Merchant merchant = Merchant.builder()
                .name("Test Merchant")
                .apiKey("test-api-key")
                .secretKey("test-secret-key")
                .email("test@example.com")
                .build();
        merchant = entityManager.persistAndFlush(merchant);

        WebhookEndpoint endpoint = WebhookEndpoint.builder()
                .merchant(merchant)
                .url("https://example.com/webhook")
                .description("Test webhook endpoint")
                .secret("test-secret")
                .retryEnabled(true)
                .maxRetries(3)
                .timeoutSeconds(30)
                .headers(null) // Set to null to avoid JSON type issues
                .build();

        // When
        WebhookEndpoint savedEndpoint = entityManager.persistAndFlush(endpoint);
        entityManager.clear();

        // Then
        WebhookEndpoint foundEndpoint = webhookEndpointRepository.findById(savedEndpoint.getId()).orElse(null);
        assertThat(foundEndpoint).isNotNull();
        assertThat(foundEndpoint.getUrl()).isEqualTo("https://example.com/webhook");
        assertThat(foundEndpoint.getMerchant().getId()).isEqualTo(merchant.getId());
    }

    @Test
    void testFindByMerchantId() {
        // Given
        Merchant merchant = Merchant.builder()
                .name("Test Merchant")
                .apiKey("test-api-key")
                .secretKey("test-secret-key")
                .email("test@example.com")
                .build();
        merchant = entityManager.persistAndFlush(merchant);

        WebhookEndpoint endpoint1 = WebhookEndpoint.builder()
                .merchant(merchant)
                .url("https://example.com/webhook1")
                .description("Test webhook endpoint 1")
                .secret("test-secret")
                .retryEnabled(true)
                .maxRetries(3)
                .timeoutSeconds(30)
                .headers(null) // Set to null to avoid JSON type issues
                .build();

        WebhookEndpoint endpoint2 = WebhookEndpoint.builder()
                .merchant(merchant)
                .url("https://example.com/webhook2")
                .description("Test webhook endpoint 2")
                .secret("test-secret")
                .retryEnabled(true)
                .maxRetries(3)
                .timeoutSeconds(30)
                .headers(null) // Set to null to avoid JSON type issues
                .build();

        entityManager.persistAndFlush(endpoint1);
        entityManager.persistAndFlush(endpoint2);
        entityManager.clear();

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<WebhookEndpoint> endpoints = webhookEndpointRepository.findByMerchantId(merchant.getId(), pageable);

        // Then
        assertThat(endpoints.getContent()).hasSize(2);
        // Compare merchant IDs instead of merchant objects to avoid circular reference issues
        assertThat(endpoints.getContent()).extracting(we -> we.getMerchant().getId()).containsOnly(merchant.getId());
    }
}