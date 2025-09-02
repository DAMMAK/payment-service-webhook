package dev.dammak.paymentservicewebhook.dto;

import dev.dammak.paymentservicewebhook.util.TestDataBuilder;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static jakarta.validation.Validation.buildDefaultValidatorFactory;

class WebhookEndpointDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void validDTO_shouldPassValidation() {
        // Given
        WebhookEndpointDTO dto = TestDataBuilder.createWebhookEndpointDTO();

        // When
        Set<ConstraintViolation<WebhookEndpointDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void dtoWithEmptyUrl_shouldFailValidation() {
        // Given
        WebhookEndpointDTO dto = TestDataBuilder.createWebhookEndpointDTO();
        dto.setUrl("");

        // When
        Set<ConstraintViolation<WebhookEndpointDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(2); // Both @NotBlank and @Pattern constraints fail
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("URL is required", "URL must start with http:// or https://");
    }

    @Test
    void dtoWithInvalidUrlFormat_shouldFailValidation() {
        // Given
        WebhookEndpointDTO dto = TestDataBuilder.createWebhookEndpointDTO();
        dto.setUrl("invalid-url");

        // When
        Set<ConstraintViolation<WebhookEndpointDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<WebhookEndpointDTO> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("URL must start with http:// or https://");
    }

    @Test
    void dtoWithEmptyEvents_shouldFailValidation() {
        // Given
        WebhookEndpointDTO dto = TestDataBuilder.createWebhookEndpointDTO();
        dto.setEvents(java.util.List.of());

        // When
        Set<ConstraintViolation<WebhookEndpointDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<WebhookEndpointDTO> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("At least one event subscription is required");
    }

    @Test
    void dtoWithInvalidMaxRetries_shouldFailValidation() {
        // Given
        WebhookEndpointDTO dto = TestDataBuilder.createWebhookEndpointDTO();
        dto.setMaxRetries(15); // Above max value of 10

        // When
        Set<ConstraintViolation<WebhookEndpointDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<WebhookEndpointDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("maxRetries");
    }

    @Test
    void dtoWithInvalidTimeoutSeconds_shouldFailValidation() {
        // Given
        WebhookEndpointDTO dto = TestDataBuilder.createWebhookEndpointDTO();
        dto.setTimeoutSeconds(500); // Above max value of 300

        // When
        Set<ConstraintViolation<WebhookEndpointDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<WebhookEndpointDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("timeoutSeconds");
    }

    @Test
    void dtoWithNullValuesForOptionalFields_shouldPassValidation() {
        // Given
        WebhookEndpointDTO dto = TestDataBuilder.createWebhookEndpointDTO();
        dto.setDescription(null);
        dto.setHeaders(null);
        dto.setRetryEnabled(null);
        dto.setMaxRetries(null);
        dto.setTimeoutSeconds(null);

        // When
        Set<ConstraintViolation<WebhookEndpointDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
    }
}