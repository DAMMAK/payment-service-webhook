package dev.dammak.paymentservicewebhook.service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

/**
 * Created By damola.adekoya on 01/09/2025
 *
 * @Author: damola.adekoya
 * @Email: adekoyafelix@gmail.com
 * @Date: 01/09/2025
 * @Project: payment-service-webhook
 */

@Service
@Slf4j
public class SignatureService {

    @Value("${webhook.security.signature-algorithm:HmacSHA256}")
    private String signatureAlgorithm;

    public String generateSignature(Map<String, Object> payload, String secret) {
        try {
            String payloadString = convertPayloadToString(payload);
            return generateHmacSignature(payloadString, secret);
        } catch (Exception e) {
            log.error("Failed to generate signature", e);
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    public boolean verifySignature(String payload, String signature, String secret) {
        try {
            String expectedSignature = generateHmacSignature(payload, secret);
            return constantTimeEquals(signature, expectedSignature);
        } catch (Exception e) {
            log.error("Failed to verify signature", e);
            return false;
        }
    }

    private String generateHmacSignature(String data, String secret)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(signatureAlgorithm);
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                signatureAlgorithm
        );
        mac.init(secretKeySpec);

        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    private String convertPayloadToString(Map<String, Object> payload) {
        // Convert payload to a consistent string representation
        // In production, use a proper JSON serializer with sorted keys
        return payload.toString();
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }

        if (a.length() != b.length()) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }

        return result == 0;
    }
}
