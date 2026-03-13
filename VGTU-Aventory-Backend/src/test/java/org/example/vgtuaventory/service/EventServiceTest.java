package org.example.vgtuaventory.service;

import org.example.vgtuaventory.dto.EventDTO;
import org.example.vgtuaventory.model.Event;
import org.example.vgtuaventory.model.User;
import org.example.vgtuaventory.repository.EventRepository;
import org.example.vgtuaventory.repository.UserRepository;
import org.example.vgtuaventory.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private UserDetails userDetails;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        eventService = new EventServiceImpl(eventRepository, userRepository);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetEventsForCurrentUser_Sorting() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        LocalDateTime now = LocalDateTime.now();
        Event pastEvent = new Event(1, user, now.minusDays(1), now.minusDays(1).plusHours(2), "Past Event");
        Event futureEvent1 = new Event(2, user, now.plusDays(1), now.plusDays(1).plusHours(2), "Future Event 1");
        Event futureEvent2 = new Event(3, user, now.plusDays(2), now.plusDays(2).plusHours(2), "Future Event 2");

        // Repository returns them in some order (e.g. ID order or as they come)
        when(eventRepository.findAllByOwnerOrderByStartDateAsc(user)).thenReturn(Arrays.asList(pastEvent, futureEvent1, futureEvent2));

        List<EventDTO> result = eventService.getEventsForCurrentUser();

        assertEquals(3, result.size());
        // Future events should come first, then past events
        assertEquals("Future Event 1", result.get(0).getDescription());
        assertEquals("Future Event 2", result.get(1).getDescription());
        assertEquals("Past Event", result.get(2).getDescription());
    }
}
