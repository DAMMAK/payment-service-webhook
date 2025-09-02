package dev.dammak.paymentservicewebhook.entity;
import jakarta.persistence.*;
import lombok.*;
/**
 * Created By damola.adekoya on 01/09/2025
 *
 * @Author: damola.adekoya
 * @Email: adekoyafelix@gmail.com
 * @Date: 01/09/2025
 * @Project: payment-service-webhook
 */



@Entity
@Table(name = "event_subscriptions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"webhook_endpoint_id", "event_type"}),
        indexes = @Index(name = "idx_subscription_active", columnList = "webhook_endpoint_id,is_active"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EventSubscription extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webhook_endpoint_id", nullable = false)
    private WebhookEndpoint webhookEndpoint;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(length = 500)
    private String description;
}
