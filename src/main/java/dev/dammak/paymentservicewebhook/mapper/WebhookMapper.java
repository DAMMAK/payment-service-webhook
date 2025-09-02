package dev.dammak.paymentservicewebhook.mapper;


import dev.dammak.paymentservicewebhook.dto.WebhookDeliveryDTO;
import dev.dammak.paymentservicewebhook.dto.WebhookEndpointDTO;
import dev.dammak.paymentservicewebhook.dto.WebhookEventDTO;
import dev.dammak.paymentservicewebhook.entity.EventSubscription;
import dev.dammak.paymentservicewebhook.entity.WebhookDelivery;
import dev.dammak.paymentservicewebhook.entity.WebhookEndpoint;
import dev.dammak.paymentservicewebhook.entity.WebhookEvent;
import org.mapstruct.*;

import java.util.List;

/**
 * Created By damola.adekoya on 01/09/2025
 *
 * @Author: damola.adekoya
 * @Email: adekoyafelix@gmail.com
 * @Date: 01/09/2025
 * @Project: payment-service-webhook
 */

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WebhookMapper {

    @Mapping(target = "events", source = "subscriptions", qualifiedByName = "subscriptionsToEvents")
    @Mapping(target = "statistics", ignore = true)
    @Mapping(target = "version", source = "apiVersion") // Map apiVersion from entity to version in DTO
    WebhookEndpointDTO toEndpointDTO(WebhookEndpoint endpoint);

    @Mapping(target = "subscriptions", ignore = true)
    @Mapping(target = "merchant", ignore = true)
    @Mapping(target = "deliveries", ignore = true)
    @Mapping(target = "apiVersion", source = "version") // Map version from DTO to apiVersion in entity
    WebhookEndpoint toEndpointEntity(WebhookEndpointDTO dto);

    @Mapping(target = "deliveryCount", expression = "java(event.getDeliveries() != null ? event.getDeliveries().size() : 0)")
    @Mapping(target = "successfulDeliveries", ignore = true)
    @Mapping(target = "failedDeliveries", ignore = true)
    WebhookEventDTO toEventDTO(WebhookEvent event);

    @Mapping(target = "deliveries", ignore = true)
    WebhookEvent toEventEntity(WebhookEventDTO dto);

    @Mapping(target = "eventId", source = "webhookEvent.id")
    @Mapping(target = "endpointId", source = "webhookEndpoint.id")
    WebhookDeliveryDTO toDeliveryDTO(WebhookDelivery delivery);

    List<WebhookEndpointDTO> toEndpointDTOList(List<WebhookEndpoint> endpoints);

    List<WebhookEventDTO> toEventDTOList(List<WebhookEvent> events);

    List<WebhookDeliveryDTO> toDeliveryDTOList(List<WebhookDelivery> deliveries);

    @Named("subscriptionsToEvents")
    default List<String> subscriptionsToEvents(List<EventSubscription> subscriptions) {
        if (subscriptions == null) return null;
        return subscriptions.stream()
                .filter(EventSubscription::getIsActive)
                .map(EventSubscription::getEventType)
                .toList();
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEndpointFromDTO(WebhookEndpointDTO dto, @MappingTarget WebhookEndpoint entity);
}