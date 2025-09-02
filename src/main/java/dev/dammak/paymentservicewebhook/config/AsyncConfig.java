package dev.dammak.paymentservicewebhook.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
/**
 * Created By damola.adekoya on 01/09/2025
 *
 * @Author: damola.adekoya
 * @Email: adekoyafelix@gmail.com
 * @Date: 01/09/2025
 * @Project: payment-service-webhook
 */


@Configuration
@EnableAsync
@EnableScheduling
@EnableRetry
public class AsyncConfig {
    @Value("${webhook.delivery.timeout:30000}")
    private int deliveryTimeout;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.connectTimeout(Duration.ofMillis(deliveryTimeout)).readTimeout(Duration.ofMillis(deliveryTimeout))
                .requestFactory(() -> {
                    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                    factory.setConnectTimeout(deliveryTimeout);
                    factory.setReadTimeout(deliveryTimeout);
                    return factory;
                })
                .build();
    }

    @Bean(name = "webhookExecutor")
    public Executor webhookExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Webhook-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
