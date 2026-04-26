package org.example.vgtuaventory.controller;

import jakarta.validation.Valid;
import org.example.vgtuaventory.dto.EventDTO;
import org.example.vgtuaventory.service.EventService;
import org.example.vgtuaventory.utils.AuthSessionAttributes;
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
    public ResponseEntity<?> getMyEvents(@RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId) {
        List<EventDTO> events = eventService.getAllEvents(currentUserId);
        
        if (events.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "No events were found.");
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.ok(events);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createEvent(@Valid @RequestBody EventDTO eventDTO, 
                                         @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId) {
        EventDTO createdEvent = eventService.createEvent(eventDTO, currentUserId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", createdEvent.getId());
        response.put("startDate", createdEvent.getStartDate());
        response.put("endDate", createdEvent.getEndDate());
        response.put("description", createdEvent.getDescription());
        response.put("message", "Event created successfully.");
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> editEvent(@PathVariable int id, @Valid @RequestBody EventDTO eventDTO,
                                       @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId) {
        try {
            EventDTO updatedEvent = eventService.updateEvent(id, eventDTO, currentUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedEvent.getId());
            response.put("startDate", updatedEvent.getStartDate());
            response.put("endDate", updatedEvent.getEndDate());
            response.put("description", updatedEvent.getDescription());
            response.put("message", "Event updated successfully.");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable int id, 
                                         @RequestAttribute(AuthSessionAttributes.CURRENT_USER_ID) int currentUserId) {
        try {
            eventService.deleteEvent(id, currentUserId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Event deleted successfully.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        }
    }
}
