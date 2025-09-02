package dev.dammak.paymentservicewebhook.config;

import dev.dammak.paymentservicewebhook.controller.WebhookEndpointController;
import dev.dammak.paymentservicewebhook.service.WebhookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookEndpointController.class)
@Import(TestConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebhookService webhookService; // Required dependency for WebhookEndpointController

    @Test
    void unauthenticatedUser_shouldBeForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/webhooks/endpoints"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MERCHANT")
    void authenticatedUser_shouldAccessWebhookEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/webhooks/endpoints")
                .header("X-Merchant-Id", UUID.randomUUID().toString()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MERCHANT")
    void authenticatedUser_shouldAccessWebhookEndpointCreation() throws Exception {
        mockMvc.perform(post("/api/v1/webhooks/endpoints")
                .header("X-Merchant-Id", UUID.randomUUID().toString())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{}")) // Empty body will cause bad request, not forbidden
                .andExpect(status().isBadRequest()); // Bad request due to missing body, not forbidden
    }
}