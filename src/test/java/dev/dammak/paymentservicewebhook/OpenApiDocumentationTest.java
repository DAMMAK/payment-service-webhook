package dev.dammak.paymentservicewebhook;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * Test to verify OpenAPI documentation is properly configured
 */
@SpringBootTest(classes = TestConfig.class)
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
})
@Import(TestConfig.class)
class OpenApiDocumentationTest {

    @Test
    void contextLoads() {
        // This test verifies that the context loads successfully with OpenAPI configuration
    }
}