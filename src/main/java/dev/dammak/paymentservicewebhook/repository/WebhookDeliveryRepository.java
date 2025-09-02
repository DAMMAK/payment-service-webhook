package dev.dammak.paymentservicewebhook.repository;
import dev.dammak.paymentservicewebhook.entity.WebhookDelivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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
public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, UUID> {

    Page<WebhookDelivery> findByWebhookEventId(UUID eventId, Pageable pageable);

    Page<WebhookDelivery> findByWebhookEndpointId(UUID endpointId, Pageable pageable);

    List<WebhookDelivery> findByStatusAndNextRetryAtBefore(
            WebhookDelivery.DeliveryStatus status,
            LocalDateTime time);

    @Query("SELECT wd FROM WebhookDelivery wd " +
            "WHERE wd.webhookEvent.id = :eventId " +
            "AND wd.webhookEndpoint.id = :endpointId " +
            "ORDER BY wd.attemptNumber DESC")
    List<WebhookDelivery> findByEventAndEndpoint(@Param("eventId") UUID eventId,
                                                 @Param("endpointId") UUID endpointId);

    @Query("SELECT AVG(wd.latencyMs) FROM WebhookDelivery wd " +
            "WHERE wd.webhookEndpoint.id = :endpointId " +
            "AND wd.status = 'SUCCESS' " +
            "AND wd.deliveredAt BETWEEN :startTime AND :endTime")
    Double getAverageLatency(@Param("endpointId") UUID endpointId,
                             @Param("startTime") LocalDateTime startTime,
                             @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(wd) FROM WebhookDelivery wd " +
            "WHERE wd.webhookEndpoint.id = :endpointId " +
            "AND wd.status = :status " +
            "AND wd.createdAt BETWEEN :startTime AND :endTime")
    long countByEndpointAndStatusAndTimeRange(@Param("endpointId") UUID endpointId,
                                              @Param("status") WebhookDelivery.DeliveryStatus status,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);
}