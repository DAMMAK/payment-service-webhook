package dev.dammak.paymentservicewebhook.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
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
@Schema(description = "Webhook delivery attempt record")
public class WebhookDeliveryDTO {

    @Schema(description = "Unique identifier of the delivery record", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Identifier of the webhook event", example = "456e7890-e89b-12d3-a456-426614174111")
    private UUID eventId;

    @Schema(description = "Identifier of the webhook endpoint", example = "789e0123-e89b-12d3-a456-426614174222")
    private UUID endpointId;

    @Schema(description = "Current status of the delivery", example = "SUCCESS")
    private String status;

    @Schema(description = "Attempt number for this delivery", example = "1")
    private Integer attemptNumber;

    @Schema(description = "HTTP response status code", example = "200")
    private Integer responseStatus;

    @Schema(description = "HTTP response body")
    private String responseBody;

    @Schema(description = "Error message if delivery failed")
    private String errorMessage;

    @Schema(description = "Timestamp when the delivery was completed")
    private LocalDateTime deliveredAt;

    @Schema(description = "Timestamp for next retry attempt")
    private LocalDateTime nextRetryAt;

    @Schema(description = "Delivery latency in milliseconds", example = "150")
    private Long latencyMs;

    @Schema(description = "Timestamp when the delivery was created")
    private LocalDateTime createdAt;
}