package dev.dammak.paymentservicewebhook.repository;
import dev.dammak.paymentservicewebhook.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    Optional<Merchant> findByApiKey(String apiKey);

    Optional<Merchant> findByEmail(String email);

    @Query("SELECT m FROM Merchant m WHERE m.apiKey = :apiKey AND m.status = 'ACTIVE'")
    Optional<Merchant> findActiveByApiKey(String apiKey);

    boolean existsByApiKey(String apiKey);

    boolean existsByEmail(String email);
}
