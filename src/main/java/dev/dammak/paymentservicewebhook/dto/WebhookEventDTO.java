package dev.dammak.paymentservicewebhook.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class WebhookEventDTO {

    private UUID id;

    @NotBlank(message = "Event type is required")
    private String eventType;

    @NotBlank(message = "Provider is required")
    private String provider;

    @NotNull(message = "Payload is required")
    private Map<String, Object> payload;

    private String idempotencyKey;

    private String status;

    private Map<String, Object> metadata;

    private LocalDateTime createdAt;

    private Integer deliveryCount;

    private Integer successfulDeliveries;

    private Integer failedDeliveries;
}