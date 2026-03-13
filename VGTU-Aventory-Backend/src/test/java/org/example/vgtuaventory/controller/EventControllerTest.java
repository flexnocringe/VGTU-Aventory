package org.example.vgtuaventory.controller;

import org.example.vgtuaventory.dto.EventDTO;
import org.example.vgtuaventory.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
    }

    @Test
    void testGetMyEvents_Empty() throws Exception {
        when(eventService.getEventsForCurrentUser()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/events/my-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No events were found."));
    }

    @Test
    void testGetMyEvents_NotEmpty() throws Exception {
        List<EventDTO> events = new ArrayList<>();
        events.add(new EventDTO(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), "Future Event"));
        
        when(eventService.getEventsForCurrentUser()).thenReturn(events);

        mockMvc.perform(get("/api/events/my-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Future Event"));
    }
}
