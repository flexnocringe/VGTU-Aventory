package org.example.vgtuaventory.service;

import org.example.vgtuaventory.dto.EventDTO;
import java.util.List;

public interface EventService {
    List<EventDTO> getAllEvents(int userId);
    EventDTO createEvent(EventDTO eventDTO, int userId);
    EventDTO updateEvent(int id, EventDTO eventDTO, int userId);
    void deleteEvent(int id, int userId);
}
