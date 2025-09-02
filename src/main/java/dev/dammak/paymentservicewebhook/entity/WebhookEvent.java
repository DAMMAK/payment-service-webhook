package dev.dammak.paymentservicewebhook.entity;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * Created By damola.adekoya on 01/09/2025
 *
 * @Author: damola.adekoya
 * @Email: adekoyafelix@gmail.com
 * @Date: 01/09/2025
 * @Project: payment-service-webhook
 */


@Entity
@Table(name = "webhook_events",
        indexes = {
                @Index(name = "idx_event_status", columnList = "status"),
                @Index(name = "idx_event_type", columnList = "event_type"),
                @Index(name = "idx_idempotency_key", columnList = "idempotency_key")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WebhookEvent extends BaseEntity {

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, length = 50)
    private String provider;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> payload;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EventStatus status = EventStatus.PENDING;

    @Column(name = "source_ip")
    private String sourceIp;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @OneToMany(mappedBy = "webhookEvent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WebhookDelivery> deliveries = new ArrayList<>();

    public enum EventStatus {
        PENDING, PROCESSING, DELIVERED, FAILED, EXPIRED
    }
}