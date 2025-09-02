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
@Table(name = "webhook_endpoints",
        indexes = {
                @Index(name = "idx_webhook_status", columnList = "status"),
                @Index(name = "idx_merchant_id", columnList = "merchant_id")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WebhookEndpoint extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String secret;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private WebhookStatus status = WebhookStatus.ACTIVE;

    @Column(name = "api_version")
    @Builder.Default
    private String apiVersion = "v1";  // Renamed from 'version' to 'apiVersion'

    @Builder.Default
    private Boolean retryEnabled = true;

    @Builder.Default
    private Integer maxRetries = 3;

    @Builder.Default
    private Integer timeoutSeconds = 30;

    // Use columnDefinition based on the database dialect
    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private Map<String, Object> headers;

    @OneToMany(mappedBy = "webhookEndpoint", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EventSubscription> subscriptions = new ArrayList<>();

    @OneToMany(mappedBy = "webhookEndpoint", fetch = FetchType.LAZY)
    private List<WebhookDelivery> deliveries;

    public enum WebhookStatus {
        ACTIVE, INACTIVE, FAILED, SUSPENDED
    }
}