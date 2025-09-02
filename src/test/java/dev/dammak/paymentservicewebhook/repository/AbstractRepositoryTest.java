package dev.dammak.paymentservicewebhook.repository;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(RepositoryTestConfig.class)
public abstract class AbstractRepositoryTest {
    // Base class for repository tests
    // Configuration is handled by application-test.yml
}