package org.example.vgtuaventory.service;

import org.example.vgtuaventory.dto.EventDTO;
import org.example.vgtuaventory.model.Event;
import org.example.vgtuaventory.repository.EventRepository;
import org.example.vgtuaventory.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        eventService = new EventServiceImpl(eventRepository);
    }

    @Test
    void testGetAllEvents_Sorting() {
        LocalDateTime now = LocalDateTime.now();
        Event pastEvent = new Event(1, null, now.minusDays(1), now.minusDays(1).plusHours(2), "Past Event");
        Event futureEvent1 = new Event(2, null, now.plusDays(1), now.plusDays(1).plusHours(2), "Future Event 1");
        Event futureEvent2 = new Event(3, null, now.plusDays(2), now.plusDays(2).plusHours(2), "Future Event 2");

        // Repository returns them in some order (e.g. ID order or as they come)
        when(eventRepository.findAllByOrderByStartDateAsc()).thenReturn(Arrays.asList(pastEvent, futureEvent1, futureEvent2));

        List<EventDTO> result = eventService.getAllEvents();

        assertEquals(3, result.size());
        // Future events should come first, then past events
        assertEquals("Future Event 1", result.get(0).getDescription());
        assertEquals("Future Event 2", result.get(1).getDescription());
        assertEquals("Past Event", result.get(2).getDescription());
    }
}
