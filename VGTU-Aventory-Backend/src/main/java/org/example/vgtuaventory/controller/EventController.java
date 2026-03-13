package org.example.vgtuaventory.controller;

import org.example.vgtuaventory.dto.EventDTO;
import org.example.vgtuaventory.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/my-events")
    public ResponseEntity<?> getMyEvents() {
        List<EventDTO> events = eventService.getEventsForCurrentUser();
        
        if (events.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "No events were found.");
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.ok(events);
    }
}
