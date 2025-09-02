package dev.dammak.paymentservicewebhook.repository;

import dev.dammak.paymentservicewebhook.entity.EventSubscription;
import dev.dammak.paymentservicewebhook.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Created By damola.adekoya on 01/09/2025
 *
 * @Author: damola.adekoya
 * @Email: adekoyafelix@gmail.com
 * @Date: 01/09/2025
 * @Project: payment-service-webhook
 */
public interface EventSubscriptionRepository extends JpaRepository<EventSubscription, UUID> {
}
