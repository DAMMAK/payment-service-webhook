package dev.dammak.paymentservicewebhook.repository;

import com.vladmihalcea.hibernate.type.json.JsonType;
import dev.dammak.paymentservicewebhook.entity.*;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.service.ServiceRegistry;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "dev.dammak.paymentservicewebhook.entity")
@EnableJpaRepositories(basePackages = "dev.dammak.paymentservicewebhook.repository")
public class RepositoryTestConfig {
    
    @Bean
    public TypeContributor jsonTypeContributor() {
        return new TypeContributor() {
            @Override
            public void contribute(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
                // Register the JsonType for Hibernate
                typeContributions.contributeType(new JsonType(), "json");
            }
        };
    }
}