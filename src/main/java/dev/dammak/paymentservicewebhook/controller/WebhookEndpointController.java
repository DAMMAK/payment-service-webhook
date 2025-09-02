package dev.dammak.paymentservicewebhook.controller;

import dev.dammak.paymentservicewebhook.dto.WebhookEndpointDTO;
import dev.dammak.paymentservicewebhook.entity.WebhookEndpoint;
import dev.dammak.paymentservicewebhook.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Created By damola.adekoya on 01/09/2025
 *
 * @Author: damola.adekoya
 * @Email: adekoyafelix@gmail.com
 * @Date: 01/09/2025
 * @Project: payment-service-webhook
 */

@RestController
@RequestMapping("/api/v1/webhooks/endpoints")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhook Endpoints", description = "Manage webhook endpoints")
public class WebhookEndpointController {

    private final WebhookService webhookService;

    @PostMapping
    @Operation(summary = "Create a new webhook endpoint")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<WebhookEndpointDTO> createEndpoint(
            @RequestHeader("X-Merchant-Id") UUID merchantId,
            @Valid @RequestBody WebhookEndpointDTO request) {

        log.info("Creating webhook endpoint for merchant: {}", merchantId);
        WebhookEndpointDTO response = webhookService.createEndpoint(merchantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get webhook endpoint by ID")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<WebhookEndpointDTO> getEndpoint(
            @RequestHeader("X-Merchant-Id") UUID merchantId,
            @PathVariable UUID id) {

        log.info("Getting webhook endpoint: {} for merchant: {}", id, merchantId);
        WebhookEndpointDTO response = webhookService.getEndpoint(merchantId, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update webhook endpoint")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<WebhookEndpointDTO> updateEndpoint(
            @RequestHeader("X-Merchant-Id") UUID merchantId,
            @PathVariable UUID id,
            @Valid @RequestBody WebhookEndpointDTO request) {

        log.info("Updating webhook endpoint: {} for merchant: {}", id, merchantId);
        WebhookEndpointDTO response = webhookService.updateEndpoint(merchantId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete webhook endpoint")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Void> deleteEndpoint(
            @RequestHeader("X-Merchant-Id") UUID merchantId,
            @PathVariable UUID id) {

        log.info("Deleting webhook endpoint: {} for merchant: {}", id, merchantId);
        webhookService.deleteEndpoint(merchantId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List webhook endpoints")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Page<WebhookEndpointDTO>> listEndpoints(
            @RequestHeader("X-Merchant-Id") UUID merchantId,
            @RequestParam(required = false) WebhookEndpoint.WebhookStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("Listing webhook endpoints for merchant: {}", merchantId);
        Page<WebhookEndpointDTO> response = webhookService.listEndpoints(merchantId, status, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/test")
    @Operation(summary = "Test webhook endpoint")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Map<String, Object>> testEndpoint(
            @RequestHeader("X-Merchant-Id") UUID merchantId,
            @PathVariable UUID id) {

        log.info("Testing webhook endpoint: {} for merchant: {}", id, merchantId);
        // Implementation for testing webhook endpoint
        Map<String, Object> response = Map.of(
                "status", "success",
                "message", "Test webhook sent successfully",
                "timestamp", System.currentTimeMillis()
        );
        return ResponseEntity.ok(response);
    }
}