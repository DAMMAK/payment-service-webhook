package dev.dammak.paymentservicewebhook.service;

import dev.dammak.paymentservicewebhook.entity.WebhookEvent;
import dev.dammak.paymentservicewebhook.repository.WebhookEventRepository;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookServiceUpdateEventStatusTest {

    @Mock
    private WebhookEventRepository eventRepository;

    @InjectMocks
    private WebhookService webhookService;

    @Test
    void updateEventStatus_shouldUpdateStatusWhenEventExists() {
        // Given
        UUID eventId = UUID.randomUUID();
        WebhookEvent.EventStatus newStatus = WebhookEvent.EventStatus.DELIVERED;
        
        when(eventRepository.existsById(eventId)).thenReturn(true);
        
        // When
        webhookService.updateEventStatus(eventId, newStatus);
        
        // Then
        verify(eventRepository).existsById(eventId);
        verify(eventRepository).updateStatus(eventId, newStatus);
    }

    @Test
    void updateEventStatus_shouldThrowExceptionWhenEventNotFound() {
        // Given
        UUID eventId = UUID.randomUUID();
        WebhookEvent.EventStatus newStatus = WebhookEvent.EventStatus.DELIVERED;
        
        when(eventRepository.existsById(eventId)).thenReturn(false);
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> webhookService.updateEventStatus(eventId, newStatus)
        );
        
        assertEquals("Webhook event not found with ID: " + eventId, exception.getMessage());
        verify(eventRepository).existsById(eventId);
        verify(eventRepository, never()).updateStatus(any(), any());
    }
}