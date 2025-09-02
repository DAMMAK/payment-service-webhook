package dev.dammak.paymentservicewebhook.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class WebhookDeliveryDTO {

    private UUID id;

    private UUID eventId;

    private UUID endpointId;

    private String status;

    private Integer attemptNumber;

    private Integer responseStatus;

    private String responseBody;

    private String errorMessage;

    private LocalDateTime deliveredAt;

    private LocalDateTime nextRetryAt;

    private Long latencyMs;

    private LocalDateTime createdAt;
}