package dev.dammak.paymentservicewebhook.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
/**
 * Created By damola.adekoya on 01/09/2025
 *
 * @Author: damola.adekoya
 * @Email: adekoyafelix@gmail.com
 * @Date: 01/09/2025
 * @Project: payment-service-webhook
 */

@Entity
@Table(name = "webhook_deliveries",
        indexes = {
                @Index(name = "idx_delivery_status", columnList = "status"),
                @Index(name = "idx_next_retry", columnList = "next_retry_at"),
                @Index(name = "idx_event_endpoint", columnList = "webhook_event_id,webhook_endpoint_id")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WebhookDelivery extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webhook_event_id", nullable = false)
    private WebhookEvent webhookEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webhook_endpoint_id", nullable = false)
    private WebhookEndpoint webhookEndpoint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Builder.Default
    private Integer attemptNumber = 1;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "latency_ms")
    private Long latencyMs;

    public enum DeliveryStatus {
        PENDING, IN_PROGRESS, SUCCESS, FAILED, RETRY_SCHEDULED, MAX_RETRIES_EXCEEDED
    }
}