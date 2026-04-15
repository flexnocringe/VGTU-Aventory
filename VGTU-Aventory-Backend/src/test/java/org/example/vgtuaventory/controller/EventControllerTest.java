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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        when(eventService.getAllEvents()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/events/my-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No events were found."));
    }

    @Test
    void testGetMyEvents_NotEmpty() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);
        List<EventDTO> events = new ArrayList<>();
        events.add(new EventDTO(start, end, "Future Event"));
        
        when(eventService.getAllEvents()).thenReturn(events);

        mockMvc.perform(get("/api/events/my-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Future Event"))
                .andExpect(jsonPath("$[0].startDate").exists())
                .andExpect(jsonPath("$[0].endDate").exists());
    }

    @Test
    void testCreateEvent_Success() throws Exception {
        EventDTO eventDTO = new EventDTO(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), "New Event");
        when(eventService.createEvent(any(EventDTO.class))).thenReturn(eventDTO);

        mockMvc.perform(post("/api/events/create")
                .contentType("application/json")
                .content("{\"startDate\":\"2026-04-16T10:00:00\", \"endDate\":\"2026-04-16T12:00:00\", \"description\":\"New Event\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("New Event"))
                .andExpect(jsonPath("$.message").value("Event created successfully."));
    }

    @Test
    void testCreateEvent_MissingDates() throws Exception {
        mockMvc.perform(post("/api/events/create")
                .contentType("application/json")
                .content("{\"description\":\"Invalid Event\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testEditEvent_Success() throws Exception {
        int eventId = 1;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);
        EventDTO updateDTO = new EventDTO(eventId, start, end, "Updated Event");
        
        when(eventService.updateEvent(any(Integer.class), any(EventDTO.class))).thenReturn(updateDTO);

        mockMvc.perform(put("/api/events/edit/" + eventId)
                .contentType("application/json")
                .content("{\"startDate\":\"2026-04-16T10:00:00\", \"endDate\":\"2026-04-16T12:00:00\", \"description\":\"Updated Event\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.description").value("Updated Event"))
                .andExpect(jsonPath("$.message").value("Event updated successfully."));
    }

    @Test
    void testEditEvent_NotFound() throws Exception {
        int eventId = 1;
        when(eventService.updateEvent(any(Integer.class), any(EventDTO.class)))
                .thenThrow(new RuntimeException("Event not found with id: " + eventId));

        mockMvc.perform(put("/api/events/edit/" + eventId)
                .contentType("application/json")
                .content("{\"startDate\":\"2026-04-16T10:00:00\", \"endDate\":\"2026-04-16T12:00:00\", \"description\":\"Updated Event\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Event not found with id: " + eventId));
    }

    @Test
    void testDeleteEvent_Success() throws Exception {
        int eventId = 1;
        doNothing().when(eventService).deleteEvent(eventId);

        mockMvc.perform(delete("/api/events/delete/" + eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Event deleted successfully."));
    }

    @Test
    void testDeleteEvent_NotFound() throws Exception {
        int eventId = 1;
        doThrow(new RuntimeException("Event not found with id: " + eventId))
                .when(eventService).deleteEvent(eventId);

        mockMvc.perform(delete("/api/events/delete/" + eventId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Event not found with id: " + eventId));
    }
}
