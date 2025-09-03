package dev.dammak.paymentservicewebhook.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
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



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Webhook endpoint configuration for receiving payment events")
public class WebhookEndpointDTO {

    @Schema(description = "Unique identifier of the webhook endpoint", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @NotBlank(message = "URL is required")
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
    @Schema(description = "URL where webhook events will be delivered", example = "https://your-service.com/webhook")
    private String url;

    @Schema(description = "Description of the webhook endpoint", example = "Payment processing webhook")
    private String description;

    @Schema(description = "Secret key used for signature verification", example = "sk_1234567890abcdef")
    private String secret;

    @Schema(description = "Current status of the webhook endpoint", example = "ACTIVE")
    private String status;

    @Schema(description = "API version of the webhook endpoint", example = "2025-09-01")
    private String version;

    @Schema(description = "Whether retry is enabled for failed deliveries", example = "true")
    private Boolean retryEnabled;

    @Min(0)
    @Max(10)
    @Schema(description = "Maximum number of retry attempts", example = "3")
    private Integer maxRetries;

    @Min(1)
    @Max(300)
    @Schema(description = "Timeout for webhook delivery in seconds", example = "30")
    private Integer timeoutSeconds;

    @Schema(description = "Custom headers to include in webhook requests")
    private Map<String, Object> headers;

    @NotEmpty(message = "At least one event subscription is required")
    @Schema(description = "List of event types to subscribe to", example = "[\"payment.created\", \"payment.updated\"]")
    private List<String> events;

    @Schema(description = "Timestamp when the endpoint was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the endpoint was last updated")
    private LocalDateTime updatedAt;

    @Schema(description = "Statistics about webhook deliveries")
    private WebhookStatisticsDTO statistics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Statistics about webhook deliveries")
    public static class WebhookStatisticsDTO {
        @Schema(description = "Total number of webhook deliveries", example = "100")
        private Long totalDeliveries;
        
        @Schema(description = "Number of successful deliveries", example = "95")
        private Long successfulDeliveries;
        
        @Schema(description = "Number of failed deliveries", example = "5")
        private Long failedDeliveries;
        
        @Schema(description = "Success rate as a percentage", example = "95.0")
        private Double successRate;
        
        @Schema(description = "Average delivery latency in milliseconds", example = "150.5")
        private Double averageLatencyMs;
        
        @Schema(description = "Timestamp of the last delivery")
        private LocalDateTime lastDeliveryAt;
    }
}