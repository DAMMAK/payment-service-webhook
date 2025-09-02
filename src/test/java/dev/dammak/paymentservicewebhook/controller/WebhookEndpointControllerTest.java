package dev.dammak.paymentservicewebhook.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dammak.paymentservicewebhook.dto.WebhookEndpointDTO;
import dev.dammak.paymentservicewebhook.entity.WebhookEndpoint;
import dev.dammak.paymentservicewebhook.service.WebhookService;
import dev.dammak.paymentservicewebhook.util.TestDataBuilder;
import dev.dammak.paymentservicewebhook.config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebhookEndpointController.class)
@Import(TestConfig.class)
class WebhookEndpointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebhookService webhookService;

    private UUID merchantId;
    private WebhookEndpointDTO endpointDTO;

    @BeforeEach
    void setUp() {
        merchantId = UUID.randomUUID();
        endpointDTO = TestDataBuilder.createWebhookEndpointDTO();
    }

    @Test
    @WithMockUser(roles = "MERCHANT")
    void createEndpoint_shouldReturnCreatedEndpoint() throws Exception {
        // Given
        when(webhookService.createEndpoint(eq(merchantId), any(WebhookEndpointDTO.class)))
                .thenReturn(endpointDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/webhooks/endpoints")
                        .header("X-Merchant-Id", merchantId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(endpointDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.url").value(endpointDTO.getUrl()));
    }

    @Test
    @WithMockUser(roles = "MERCHANT")
    void getEndpoint_shouldReturnEndpoint() throws Exception {
        // Given
        UUID endpointId = UUID.randomUUID();
        when(webhookService.getEndpoint(merchantId, endpointId)).thenReturn(endpointDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/webhooks/endpoints/{id}", endpointId)
                        .header("X-Merchant-Id", merchantId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.url").value(endpointDTO.getUrl()));
    }

    @Test
    @WithMockUser(roles = "MERCHANT")
    void updateEndpoint_shouldReturnUpdatedEndpoint() throws Exception {
        // Given
        UUID endpointId = UUID.randomUUID();
        when(webhookService.updateEndpoint(eq(merchantId), eq(endpointId), any(WebhookEndpointDTO.class)))
                .thenReturn(endpointDTO);

        // When & Then
        mockMvc.perform(put("/api/v1/webhooks/endpoints/{id}", endpointId)
                        .header("X-Merchant-Id", merchantId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(endpointDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.url").value(endpointDTO.getUrl()));
    }

    @Test
    @WithMockUser(roles = "MERCHANT")
    void deleteEndpoint_shouldReturnNoContent() throws Exception {
        // Given
        UUID endpointId = UUID.randomUUID();
        // When & Then
        mockMvc.perform(delete("/api/v1/webhooks/endpoints/{id}", endpointId)
                        .header("X-Merchant-Id", merchantId.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "MERCHANT")
    void listEndpoints_shouldReturnEndpointsPage() throws Exception {
        // Given
        Page<WebhookEndpointDTO> page = new PageImpl<>(List.of(endpointDTO));
        when(webhookService.listEndpoints(eq(merchantId), any(), any()))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/webhooks/endpoints")
                        .header("X-Merchant-Id", merchantId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].url").value(endpointDTO.getUrl()));
    }

    @Test
    @WithMockUser(roles = "MERCHANT")
    void testEndpoint_shouldReturnSuccessResponse() throws Exception {
        // Given
        UUID endpointId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(post("/api/v1/webhooks/endpoints/{id}/test", endpointId)
                        .header("X-Merchant-Id", merchantId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @WithMockUser(roles = "MERCHANT")
    void createEndpoint_shouldReturnBadRequestForInvalidData() throws Exception {
        // Given
        WebhookEndpointDTO invalidDTO = TestDataBuilder.createWebhookEndpointDTO();
        invalidDTO.setUrl("invalid-url"); // Invalid URL format
        invalidDTO.setEvents(List.of()); // Empty events list

        // When & Then
        mockMvc.perform(post("/api/v1/webhooks/endpoints")
                        .header("X-Merchant-Id", merchantId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }
}