package dev.dammak.paymentservicewebhook.repository;
import dev.dammak.paymentservicewebhook.entity.WebhookEndpoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created By damola.adekoya on 01/09/2025
 *
 * @Author: damola.adekoya
 * @Email: adekoyafelix@gmail.com
 * @Date: 01/09/2025
 * @Project: payment-service-webhook
 */

@Repository
public interface WebhookEndpointRepository extends JpaRepository<WebhookEndpoint, UUID> {

    Page<WebhookEndpoint> findByMerchantId(UUID merchantId, Pageable pageable);

    Page<WebhookEndpoint> findByMerchantIdAndStatus(UUID merchantId,
                                                    WebhookEndpoint.WebhookStatus status,
                                                    Pageable pageable);

    @Query("SELECT DISTINCT we FROM WebhookEndpoint we " +
            "JOIN FETCH we.subscriptions s " +
            "WHERE s.eventType = :eventType " +
            "AND s.isActive = true " +
            "AND we.status = 'ACTIVE'")
    List<WebhookEndpoint> findActiveEndpointsByEventType(@Param("eventType") String eventType);

    @Query("SELECT we FROM WebhookEndpoint we " +
            "JOIN FETCH we.merchant " +
            "WHERE we.id = :id")
    Optional<WebhookEndpoint> findByIdWithMerchant(@Param("id") UUID id);

    List<WebhookEndpoint> findByStatus(WebhookEndpoint.WebhookStatus status);

    long countByMerchantIdAndStatus(UUID merchantId, WebhookEndpoint.WebhookStatus status);
}
