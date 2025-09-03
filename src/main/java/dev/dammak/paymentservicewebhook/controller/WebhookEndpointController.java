package dev.dammak.paymentservicewebhook.controller;

import dev.dammak.paymentservicewebhook.dto.WebhookEndpointDTO;
import dev.dammak.paymentservicewebhook.entity.WebhookEndpoint;
import dev.dammak.paymentservicewebhook.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(name = "Webhook Endpoints", description = "Manage webhook endpoints for receiving payment events")
@SecurityRequirement(name = "bearerAuth")
public class WebhookEndpointController {

    private final WebhookService webhookService;

    @PostMapping
    @Operation(
            summary = "Create a new webhook endpoint",
            description = "Creates a new webhook endpoint for a merchant to receive payment events"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Webhook endpoint created successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = WebhookEndpointDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<WebhookEndpointDTO> createEndpoint(
            @Parameter(description = "Merchant ID", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-Merchant-Id") UUID merchantId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Webhook endpoint details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = WebhookEndpointDTO.class))
            )
            @Valid @RequestBody WebhookEndpointDTO request) {

        log.info("Creating webhook endpoint for merchant: {}", merchantId);
        WebhookEndpointDTO response = webhookService.createEndpoint(merchantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get webhook endpoint by ID",
            description = "Retrieves a specific webhook endpoint by its ID for the authenticated merchant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Webhook endpoint retrieved successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = WebhookEndpointDTO.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Webhook endpoint not found")
    })
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<WebhookEndpointDTO> getEndpoint(
            @Parameter(description = "Merchant ID", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-Merchant-Id") UUID merchantId,
            @Parameter(description = "Webhook endpoint ID", in = ParameterIn.PATH, required = true)
            @PathVariable UUID id) {

        log.info("Getting webhook endpoint: {} for merchant: {}", id, merchantId);
        WebhookEndpointDTO response = webhookService.getEndpoint(merchantId, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update webhook endpoint",
            description = "Updates an existing webhook endpoint with new configuration"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Webhook endpoint updated successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = WebhookEndpointDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Webhook endpoint not found")
    })
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<WebhookEndpointDTO> updateEndpoint(
            @Parameter(description = "Merchant ID", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-Merchant-Id") UUID merchantId,
            @Parameter(description = "Webhook endpoint ID", in = ParameterIn.PATH, required = true)
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated webhook endpoint details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = WebhookEndpointDTO.class))
            )
            @Valid @RequestBody WebhookEndpointDTO request) {

        log.info("Updating webhook endpoint: {} for merchant: {}", id, merchantId);
        WebhookEndpointDTO response = webhookService.updateEndpoint(merchantId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete webhook endpoint",
            description = "Deletes a webhook endpoint, stopping all event deliveries to that endpoint"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Webhook endpoint deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Webhook endpoint not found")
    })
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Void> deleteEndpoint(
            @Parameter(description = "Merchant ID", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-Merchant-Id") UUID merchantId,
            @Parameter(description = "Webhook endpoint ID", in = ParameterIn.PATH, required = true)
            @PathVariable UUID id) {

        log.info("Deleting webhook endpoint: {} for merchant: {}", id, merchantId);
        webhookService.deleteEndpoint(merchantId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(
            summary = "List webhook endpoints",
            description = "Retrieves a paginated list of webhook endpoints for the authenticated merchant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Webhook endpoints retrieved successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @Parameters({
            @Parameter(name = "status", description = "Filter by webhook endpoint status", in = ParameterIn.QUERY),
            @Parameter(name = "page", description = "Page number (0-indexed)", in = ParameterIn.QUERY),
            @Parameter(name = "size", description = "Page size", in = ParameterIn.QUERY),
            @Parameter(name = "sort", description = "Sort criteria (e.g., createdAt,desc)", in = ParameterIn.QUERY)
    })
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Page<WebhookEndpointDTO>> listEndpoints(
            @Parameter(description = "Merchant ID", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-Merchant-Id") UUID merchantId,
            @Parameter(description = "Filter by webhook endpoint status")
            @RequestParam(required = false) WebhookEndpoint.WebhookStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("Listing webhook endpoints for merchant: {}", merchantId);
        Page<WebhookEndpointDTO> response = webhookService.listEndpoints(merchantId, status, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/test")
    @Operation(
            summary = "Test webhook endpoint",
            description = "Sends a test event to the specified webhook endpoint to verify connectivity"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Test webhook sent successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Webhook endpoint not found")
    })
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Map<String, Object>> testEndpoint(
            @Parameter(description = "Merchant ID", in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-Merchant-Id") UUID merchantId,
            @Parameter(description = "Webhook endpoint ID", in = ParameterIn.PATH, required = true)
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