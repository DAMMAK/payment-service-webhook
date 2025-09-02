package dev.dammak.paymentservicewebhook.exception;

/**
 * Created By damola.adekoya on 01/09/2025
 *
 * @Author: damola.adekoya
 * @Email: adekoyafelix@gmail.com
 * @Date: 01/09/2025
 * @Project: payment-service-webhook
 */

public class WebhookDeliveryException extends RuntimeException {

    public WebhookDeliveryException(String message) {
        super(message);
    }

    public WebhookDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}