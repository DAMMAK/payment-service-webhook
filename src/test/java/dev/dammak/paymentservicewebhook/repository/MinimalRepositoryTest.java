package dev.dammak.paymentservicewebhook.repository;

import dev.dammak.paymentservicewebhook.entity.Merchant;
import dev.dammak.paymentservicewebhook.entity.WebhookEndpoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(RepositoryTestConfig.class)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=true"
})
class MinimalRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testMerchantEntityCreation() {
        // Given
        Merchant merchant = Merchant.builder()
                .name("Test Merchant")
                .apiKey("test-api-key")
                .secretKey("test-secret-key")
                .email("test@example.com")
                .build();

        // When
        Merchant savedMerchant = entityManager.persistAndFlush(merchant);
        
        // Then
        assertThat(savedMerchant.getId()).isNotNull();
    }

    @Test
    void testWebhookEndpointEntityCreation() {
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
        
        // Then
        assertThat(savedEndpoint.getId()).isNotNull();
        assertThat(savedEndpoint.getMerchant().getId()).isEqualTo(merchant.getId());
    }
}