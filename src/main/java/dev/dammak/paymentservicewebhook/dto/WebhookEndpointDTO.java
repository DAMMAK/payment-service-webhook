package dev.dammak.paymentservicewebhook.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
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
public class WebhookEndpointDTO {

    private UUID id;

    @NotBlank(message = "URL is required")
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
    private String url;

    private String description;

    private String secret;

    private String status;

    private String version;

    private Boolean retryEnabled;

    @Min(0)
    @Max(10)
    private Integer maxRetries;

    @Min(1)
    @Max(300)
    private Integer timeoutSeconds;

    private Map<String, Object> headers;

    @NotEmpty(message = "At least one event subscription is required")
    private List<String> events;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private WebhookStatisticsDTO statistics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebhookStatisticsDTO {
        private Long totalDeliveries;
        private Long successfulDeliveries;
        private Long failedDeliveries;
        private Double successRate;
        private Double averageLatencyMs;
        private LocalDateTime lastDeliveryAt;
    }
}