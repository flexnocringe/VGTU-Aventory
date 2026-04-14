package org.example.vgtuaventory.controller;

import jakarta.validation.Valid;
import org.example.vgtuaventory.dto.EventDTO;
import org.example.vgtuaventory.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
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
        List<EventDTO> events = eventService.getAllEvents();
        
        if (events.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "No events were found.");
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.ok(events);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createEvent(@Valid @RequestBody EventDTO eventDTO) {
        EventDTO createdEvent = eventService.createEvent(eventDTO);
        
        Map<String, Object> response = new HashMap<>();
        response.put("startDate", createdEvent.getStartDate());
        response.put("endDate", createdEvent.getEndDate());
        response.put("description", createdEvent.getDescription());
        response.put("message", "Event created successfully.");
        
        return ResponseEntity.ok(response);
    }
}
