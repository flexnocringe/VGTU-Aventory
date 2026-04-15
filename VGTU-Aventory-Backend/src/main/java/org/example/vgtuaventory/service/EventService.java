package org.example.vgtuaventory.service;

import org.example.vgtuaventory.dto.EventDTO;
import java.util.List;

public interface EventService {
    List<EventDTO> getAllEvents();
    EventDTO createEvent(EventDTO eventDTO);
    EventDTO updateEvent(int id, EventDTO eventDTO);
    void deleteEvent(int id);
}
