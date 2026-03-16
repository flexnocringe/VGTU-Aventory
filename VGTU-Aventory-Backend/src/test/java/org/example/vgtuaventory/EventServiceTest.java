package org.example.vgtuaventory;

import org.example.vgtuaventory.repository.EventRepository;
import org.example.vgtuaventory.service.EventService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class EventServiceTest {

    private final EventRepository eventRepository = mock(EventRepository.class);
    private final EventService eventService = new EventService(eventRepository);

    @Test
    void deleteEvent_shouldCallRepositoryDeleteById() {
        Integer eventId = 1;

        when(eventRepository.existsById(eventId)).thenReturn(true);

        eventService.deleteEvent(eventId);

        verify(eventRepository).deleteById(eventId);
    }

    @Test
    void deleteEvent_shouldCallRepositoryDeleteByIdOnce() {
        Integer eventId = 5;

        when(eventRepository.existsById(eventId)).thenReturn(true);

        eventService.deleteEvent(eventId);

        verify(eventRepository, times(1)).deleteById(eventId);
    }

    @Test
    void deleteEvent_shouldWorkForAnyId() {
        Integer eventId = 999;

        when(eventRepository.existsById(eventId)).thenReturn(true);

        eventService.deleteEvent(eventId);

        verify(eventRepository).deleteById(eventId);
    }
}