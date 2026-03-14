package org.example.vgtuaventory.service;

import org.example.vgtuaventory.repository.EventRepository;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public void deleteEvent(int id) {
        eventRepository.deleteById(id);
    }
}