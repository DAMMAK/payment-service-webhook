package dev.dammak.paymentservicewebhook;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DATABASE_TO_UPPER=false;CASE_INSENSITIVE_IDENTIFIERS=TRUE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.flyway.enabled=false",
    "spring.jpa.generate-ddl=true"
})
class PaymentServiceWebhookApplicationTests {

    @Test
    void contextLoads() {
    }

}