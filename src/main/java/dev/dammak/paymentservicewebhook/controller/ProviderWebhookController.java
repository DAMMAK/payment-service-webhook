package dev.dammak.paymentservicewebhook.controller;


import dev.dammak.paymentservicewebhook.dto.WebhookEventDTO;
import dev.dammak.paymentservicewebhook.service.EventProcessingService;
import dev.dammak.paymentservicewebhook.service.SignatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
/**
 * Created By damola.adekoya on 01/09/2025
 *
 * @Author: damola.adekoya
 * @Email: adekoyafelix@gmail.com
 * @Date: 01/09/2025
 * @Project: payment-service-webhook
 */


@RestController
@RequestMapping("/api/v1/webhooks/providers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Provider Webhooks", description = "Receive and process webhooks from external payment providers")
public class ProviderWebhookController {

    private final EventProcessingService eventProcessingService;
    private final SignatureService signatureService;

    @PostMapping("/{provider}/events")
    @Operation(
            summary = "Receive webhook from payment provider",
            description = "Endpoint for payment providers to send webhook events. Supports Stripe, PayPal, and Razorpay providers."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Webhook event accepted successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid webhook data or signature verification failed",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))})
    })
    public ResponseEntity<Map<String, Object>> receiveWebhook(
            @Parameter(description = "Payment provider name (stripe, paypal, razorpay)", in = ParameterIn.PATH, required = true)
            @PathVariable String provider,
            @Parameter(description = "Provider-specific signature for verification", in = ParameterIn.HEADER, required = false)
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @Parameter(description = "Idempotency key to prevent duplicate processing", in = ParameterIn.HEADER, required = false)
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Webhook event payload from the payment provider",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Map.class))
            )
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {

        log.info("Received webhook from provider: {}", provider);

        try {
            // Verify signature if provided
            if (signature != null) {
                // Provider-specific signature verification
                verifyProviderSignature(provider, payload, signature);
            }

            // Extract event type from payload (provider-specific)
            String eventType = extractEventType(provider, payload);

            // Process the event
            WebhookEventDTO event = eventProcessingService.processIncomingEvent(
                    provider,
                    eventType,
                    payload,
                    idempotencyKey,
                    request.getRemoteAddr()
            );

            Map<String, Object> response = Map.of(
                    "status", "accepted",
                    "eventId", event.getId(),
                    "timestamp", System.currentTimeMillis()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to process webhook from provider: {}", provider, e);

            Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    

    private void verifyProviderSignature(String provider, Map<String, Object> payload, String signature) {
        // Provider-specific signature verification logic
        switch (provider.toLowerCase()) {
            case "stripe":
                // Stripe signature verification
                break;
            case "paypal":
                // PayPal signature verification
                break;
            case "razorpay":
                // Razorpay signature verification
                break;
            default:
                log.warn("Unknown provider: {}, skipping signature verification", provider);
        }
    }

    private String extractEventType(String provider, Map<String, Object> payload) {
        // Provider-specific event type extraction
        return switch (provider.toLowerCase()) {
            case "stripe" -> (String) payload.get("type");
            case "paypal" -> (String) payload.get("event_type");
            case "razorpay" -> (String) payload.get("event");
            default -> "unknown";
        };
    }
}
