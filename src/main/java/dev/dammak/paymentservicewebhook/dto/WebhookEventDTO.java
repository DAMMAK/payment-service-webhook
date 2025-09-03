package dev.dammak.paymentservicewebhook.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
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
@Schema(description = "Webhook event received from payment providers")
public class WebhookEventDTO {

    @Schema(description = "Unique identifier of the webhook event", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @NotBlank(message = "Event type is required")
    @Schema(description = "Type of the event", example = "payment.created")
    private String eventType;

    @NotBlank(message = "Provider is required")
    @Schema(description = "Payment provider that sent the event", example = "stripe")
    private String provider;

    @NotNull(message = "Payload is required")
    @Schema(description = "Event payload data from the provider")
    private Map<String, Object> payload;

    @Schema(description = "Idempotency key to prevent duplicate processing", example = "idemp_1234567890")
    private String idempotencyKey;

    @Schema(description = "Current status of the event", example = "PROCESSED")
    private String status;

    @Schema(description = "Additional metadata about the event")
    private Map<String, Object> metadata;

    @Schema(description = "Timestamp when the event was created")
    private LocalDateTime createdAt;

    @Schema(description = "Number of delivery attempts", example = "1")
    private Integer deliveryCount;

    @Schema(description = "Number of successful deliveries", example = "1")
    private Integer successfulDeliveries;

    @Schema(description = "Number of failed deliveries", example = "0")
    private Integer failedDeliveries;
}