package dev.dammak.paymentservicewebhook.repository;

import dev.dammak.paymentservicewebhook.entity.WebhookEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, UUID> {

    Optional<WebhookEvent> findByIdempotencyKey(String idempotencyKey);

    Page<WebhookEvent> findByStatus(WebhookEvent.EventStatus status, Pageable pageable);

    Page<WebhookEvent> findByEventType(String eventType, Pageable pageable);

    Page<WebhookEvent> findByProvider(String provider, Pageable pageable);

    @Query("SELECT we FROM WebhookEvent we WHERE we.status = 'PENDING' " +
            "AND we.createdAt < :cutoffTime")
    List<WebhookEvent> findStaleEvents(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Modifying
    @Query("UPDATE WebhookEvent we SET we.status = :status WHERE we.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("status") WebhookEvent.EventStatus status);

    @Query("SELECT COUNT(we) FROM WebhookEvent we WHERE we.status = :status " +
            "AND we.createdAt BETWEEN :startTime AND :endTime")
    long countByStatusAndTimeRange(@Param("status") WebhookEvent.EventStatus status,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);
}