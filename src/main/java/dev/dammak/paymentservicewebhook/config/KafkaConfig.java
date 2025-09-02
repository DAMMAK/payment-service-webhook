package dev.dammak.paymentservicewebhook.config;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Created By damola.adekoya on 01/09/2025
 *
 * @Author: damola.adekoya
 * @Email: adekoyafelix@gmail.com
 * @Date: 01/09/2025
 * @Project: payment-service-webhook
 */


@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public NewTopic webhookDeliveriesTopic() {
        return TopicBuilder.name("webhook-deliveries")
                .partitions(10)
                .replicas(1)
                .config("retention.ms", "604800000") // 7 days
                .build();
    }

    @Bean
    public NewTopic webhookEventsTopic() {
        return TopicBuilder.name("webhook-events")
                .partitions(10)
                .replicas(1)
                .config("retention.ms", "604800000")
                .build();
    }

    @Bean
    public NewTopic dlqTopic() {
        return TopicBuilder.name("webhook-dlq")
                .partitions(5)
                .replicas(1)
                .config("retention.ms", "2592000000") // 30 days
                .build();
    }
}
