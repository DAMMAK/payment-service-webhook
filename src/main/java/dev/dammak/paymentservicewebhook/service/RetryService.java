package dev.dammak.paymentservicewebhook.service;

import dev.dammak.paymentservicewebhook.entity.WebhookDelivery;
import dev.dammak.paymentservicewebhook.repository.WebhookDeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created By damola.adekoya on 01/09/2025
 *
 * @Author: damola.adekoya
 * @Email: adekoyafelix@gmail.com
 * @Date: 01/09/2025
 * @Project: payment-service-webhook
 */

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RetryService {

    private final WebhookDeliveryRepository deliveryRepository;
    private final DeliveryService deliveryService;

    @Scheduled(fixedDelayString = "${webhook.retry.scheduler-interval:60000}")
    public void processRetries() {
        log.debug("Processing webhook retries");

        LocalDateTime now = LocalDateTime.now();
        List<WebhookDelivery> retriesToProcess = deliveryRepository
                .findByStatusAndNextRetryAtBefore(
                        WebhookDelivery.DeliveryStatus.RETRY_SCHEDULED,
                        now
                );

        if (!retriesToProcess.isEmpty()) {
            log.info("Found {} deliveries to retry", retriesToProcess.size());

            for (WebhookDelivery delivery : retriesToProcess) {
                try {
                    deliveryService.deliverWebhook(
                            delivery.getWebhookEvent().getId(),
                            delivery.getWebhookEndpoint().getId()
                    );
                } catch (Exception e) {
                    log.error("Failed to retry delivery {}: {}",
                            delivery.getId(), e.getMessage());
                }
            }
        }
    }
}