package dev.dammak.paymentservicewebhook.entity;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created By damola.adekoya on 01/09/2025
 *
 * @Author: damola.adekoya
 * @Email: adekoyafelix@gmail.com
 * @Date: 01/09/2025
 * @Project: payment-service-webhook
 */


@Entity
@Table(name = "merchants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Merchant extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String apiKey;

    @Column(nullable = false)
    private String secretKey;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MerchantStatus status = MerchantStatus.ACTIVE;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private String email;

    @Column
    private String webhookSecret;

    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WebhookEndpoint> webhookEndpoints = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "merchant_ip_whitelist", joinColumns = @JoinColumn(name = "merchant_id"))
    @Column(name = "ip_address")
    private List<String> ipWhitelist;

    public enum MerchantStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }
}