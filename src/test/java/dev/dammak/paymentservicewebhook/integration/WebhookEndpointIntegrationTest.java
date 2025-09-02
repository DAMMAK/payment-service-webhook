package dev.dammak.paymentservicewebhook.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dammak.paymentservicewebhook.dto.WebhookEndpointDTO;
import dev.dammak.paymentservicewebhook.util.TestDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
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
class WebhookEndpointIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "MERCHANT")
    void testEndpoint_shouldReturnSuccessResponse() throws Exception {
        // Given
        UUID merchantId = UUID.randomUUID();
        UUID endpointId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(post("/api/v1/webhooks/endpoints/{id}/test", endpointId)
                        .header("X-Merchant-Id", merchantId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Test webhook sent successfully"));
    }
}